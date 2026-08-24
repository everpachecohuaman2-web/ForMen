package com.formen.service;

import com.formen.entity.*;
import com.formen.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final CarritoService carritoService;

    public VentaService(VentaRepository ventaRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository,
                        MovimientoInventarioRepository movimientoRepository, CarritoService carritoService) {
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.carritoService = carritoService;
    }

    @Transactional
    public Venta crearVenta(HttpSession session, Principal principal, MetodoPago metodoPago, String codigoOperacion, MultipartFile captura) throws IOException {
        if (principal == null) throw new IllegalArgumentException("Debes iniciar sesión para comprar.");
        if (codigoOperacion == null || codigoOperacion.trim().isEmpty()) throw new IllegalArgumentException("El código de operación es obligatorio.");
        codigoOperacion = codigoOperacion.trim();
        if (codigoOperacion.length() < 3 || codigoOperacion.length() > 30) throw new IllegalArgumentException("El código de operación no tiene longitud válida.");
        if (codigoOperacion.contains("-") || codigoOperacion.contains("+") || !codigoOperacion.matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("El código de operación no puede ser negativo ni contener signos. Usa solo letras y números.");
        }
        if (ventaRepository.existsByCodigoOperacion(codigoOperacion)) throw new IllegalArgumentException("El código de operación ya fue utilizado.");
        if (captura == null || captura.isEmpty()) throw new IllegalArgumentException("La captura de pago es obligatoria.");
        String original = captura.getOriginalFilename() == null ? "" : captura.getOriginalFilename().toLowerCase();
        if (!(original.endsWith(".jpg") || original.endsWith(".jpeg") || original.endsWith(".png") || original.endsWith(".webp"))) {
            throw new IllegalArgumentException("Solo se permite JPG, JPEG, PNG o WEBP.");
        }
        if (captura.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("La captura no debe superar los 5MB.");

        List<ItemCarrito> items = carritoService.obtener(session);
        if (items.isEmpty()) throw new IllegalArgumentException("El carrito está vacío.");
        Usuario cliente = usuarioRepository.findByCorreo(principal.getName()).orElseThrow();
        if (!cliente.isActivo()) throw new IllegalArgumentException("Tu cuenta está inactiva. No puedes realizar compras.");

        Venta venta = new Venta();
        venta.setNumero(generarNumero());
        venta.setCliente(cliente);
        venta.setMetodoPago(metodoPago);
        venta.setCodigoOperacion(codigoOperacion);
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setCapturaPago(guardarCaptura(captura));

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : items) {
            Producto productoBD = productoRepository.findById(item.getProducto().getId()).orElseThrow();
            if (!productoBD.isActivo() || !productoBD.getCategoria().isActivo()) throw new IllegalArgumentException("El producto " + productoBD.getNombre() + " ya no está disponible.");
            if (item.getCantidad() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            if (item.getCantidad() > productoBD.getStock()) throw new IllegalArgumentException("Stock insuficiente para " + productoBD.getNombre());
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(productoBD);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(productoBD.getPrecio());
            detalle.setSubtotal(productoBD.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
            venta.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }
        venta.setSubtotal(total);
        venta.setTotal(total);
        Venta guardada = ventaRepository.save(venta);
        carritoService.vaciar(session);
        return guardada;
    }

    @Transactional
    public void aprobar(Long id, String usuarioAdmin) {
        Venta venta = ventaRepository.findById(id).orElseThrow();
        if (venta.getEstado() != EstadoVenta.PENDIENTE) throw new IllegalArgumentException("Solo se pueden aprobar ventas pendientes.");
        for (DetalleVenta d : venta.getDetalles()) {
            Producto p = productoRepository.findById(d.getProducto().getId()).orElseThrow();
            if (d.getCantidad() > p.getStock()) throw new IllegalArgumentException("Stock insuficiente para aprobar: " + p.getNombre());
        }
        for (DetalleVenta d : venta.getDetalles()) {
            Producto p = d.getProducto();
            int antes = p.getStock();
            p.setStock(antes - d.getCantidad());
            productoRepository.save(p);
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(p);
            mov.setTipo("SALIDA");
            mov.setCantidad(d.getCantidad());
            mov.setStockAntes(antes);
            mov.setStockDespues(p.getStock());
            mov.setMotivo("Venta aprobada " + venta.getNumero());
            mov.setUsuario(usuarioAdmin);
            movimientoRepository.save(mov);
        }
        venta.setEstado(EstadoVenta.APROBADA);
        venta.setFechaRevision(LocalDateTime.now());
        venta.setRevisadoPor(usuarioAdmin);
        ventaRepository.save(venta);
    }

    @Transactional
    public void rechazar(Long id, String observacion, String usuarioAdmin) {
        Venta venta = ventaRepository.findById(id).orElseThrow();
        if (venta.getEstado() != EstadoVenta.PENDIENTE) throw new IllegalArgumentException("Solo se pueden rechazar ventas pendientes.");
        venta.setEstado(EstadoVenta.RECHAZADA);
        venta.setObservacion(observacion);
        venta.setFechaRevision(LocalDateTime.now());
        venta.setRevisadoPor(usuarioAdmin);
        ventaRepository.save(venta);
    }

    private String generarNumero() {
        Long siguiente = ventaRepository.findTopByOrderByIdDesc().map(v -> v.getId() + 1).orElse(1L);
        return String.format("V%06d", siguiente);
    }

    private String guardarCaptura(MultipartFile archivo) throws IOException {
        Path dir = Path.of("uploads", "pagos");
        Files.createDirectories(dir);
        String original = archivo.getOriginalFilename() == null ? "pago.png" : archivo.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".png";
        String nombre = UUID.randomUUID() + ext;
        Files.copy(archivo.getInputStream(), dir.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/pagos/" + nombre;
    }
}
