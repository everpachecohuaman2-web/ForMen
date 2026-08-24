package com.formen.controller;

import com.formen.entity.*;
import com.formen.repository.*;
import com.formen.service.VentaService;
import org.springframework.stereotype.Controller;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ConfiguracionTiendaRepository configuracionRepository;
    private final VentaService ventaService;

    public AdminController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository,
                           VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository, MovimientoInventarioRepository movimientoRepository,
                           ConfiguracionTiendaRepository configuracionRepository, VentaService ventaService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.configuracionRepository = configuracionRepository;
        this.ventaService = ventaService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        cargarResumen(model);
        return "admin/dashboard";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        cargarResumen(model);
        model.addAttribute("ventas", ventaRepository.findAllByOrderByFechaVentaDesc());
        return "admin/reportes";
    }

    @GetMapping("/usuarios-sistema")
    public String usuariosSistema(Model model) {
        var usuariosSistema = usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getRol() != Rol.CLIENTE)
                .toList();
        model.addAttribute("usuariosSistema", usuariosSistema);
        return "admin/usuarios-sistema";
    }

    private void cargarResumen(Model model) {
        model.addAttribute("totalVentas", ventaRepository.totalVentasAprobadas());
        model.addAttribute("pendientes", ventaRepository.countByEstado(EstadoVenta.PENDIENTE));
        model.addAttribute("aprobadas", ventaRepository.countByEstado(EstadoVenta.APROBADA));
        model.addAttribute("rechazadas", ventaRepository.countByEstado(EstadoVenta.RECHAZADA));
        model.addAttribute("productosActivos", productoRepository.countByActivoTrue());
        model.addAttribute("clientes", usuarioRepository.countByRol(Rol.CLIENTE));
        model.addAttribute("ventasRecientes", ventaRepository.findTop10ByOrderByFechaVentaDesc());
    }

    @GetMapping("/productos")
    public String productos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/producto-form";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoRepository.findById(id).orElseThrow());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "admin/producto-form";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  @RequestParam Long categoriaId,
                                  @RequestParam(name = "imagenArchivo", required = false) MultipartFile imagenArchivo,
                                  Principal principal,
                                  RedirectAttributes redirect) {
        try {
            validarProducto(producto);

            Producto productoAnterior = null;
            Integer stockAntes = 0;
            boolean esNuevo = producto.getId() == null;

            if (!esNuevo) {
                productoAnterior = productoRepository.findById(producto.getId()).orElseThrow();
                stockAntes = productoAnterior.getStock() == null ? 0 : productoAnterior.getStock();
                producto.setCreadoEn(productoAnterior.getCreadoEn());

                if ((producto.getImagen() == null || producto.getImagen().isBlank())
                        && productoAnterior.getImagen() != null) {
                    producto.setImagen(productoAnterior.getImagen());
                }
            }

            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                producto.setImagen(guardarImagenProducto(imagenArchivo));
            }

            if (producto.getImagen() == null || producto.getImagen().isBlank()) {
                throw new IllegalArgumentException("La imagen es obligatoria. Sube una imagen del producto.");
            }

            Categoria cat = categoriaRepository.findById(categoriaId).orElseThrow();
            producto.setCategoria(cat);

            if (esNuevo) {
                producto.setCreadoEn(LocalDateTime.now());
            }
            producto.setActualizadoEn(LocalDateTime.now());
            Producto guardado = productoRepository.save(producto);

            registrarMovimientoProductoSiCambioStock(guardado, stockAntes, esNuevo, principal);

            redirect.addFlashAttribute("exito", "Producto guardado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            if (producto.getId() == null) {
                return "redirect:/admin/productos/nuevo";
            }
            return "redirect:/admin/productos/editar/" + producto.getId();
        }
        return "redirect:/admin/productos";
    }

    private void registrarMovimientoProductoSiCambioStock(Producto producto, Integer stockAntes, boolean esNuevo, Principal principal) {
        int antes = stockAntes == null ? 0 : stockAntes;
        int despues = producto.getStock() == null ? 0 : producto.getStock();

        if (esNuevo && despues <= 0) {
            return;
        }
        if (!esNuevo && antes == despues) {
            return;
        }

        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setStockAntes(antes);
        mov.setStockDespues(despues);
        mov.setUsuario(principal != null ? principal.getName() : "SISTEMA");

        if (esNuevo) {
            mov.setTipo("INICIAL");
            mov.setCantidad(despues);
            mov.setMotivo("Stock inicial al crear producto");
        } else if (despues > antes) {
            mov.setTipo("AJUSTE_ENTRADA");
            mov.setCantidad(despues - antes);
            mov.setMotivo("Ajuste de stock desde edición de producto");
        } else {
            mov.setTipo("AJUSTE_SALIDA");
            mov.setCantidad(antes - despues);
            mov.setMotivo("Ajuste de stock desde edición de producto");
        }

        movimientoRepository.save(mov);
    }

    private void validarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (producto.getDescripcion() == null || producto.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción del producto es obligatoria.");
        }
        if (producto.getPrecio() == null || producto.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (producto.getStockMinimo() == null || producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
    }

    private String guardarImagenProducto(MultipartFile archivo) throws Exception {
        long maximoBytes = 5L * 1024L * 1024L;
        if (archivo.getSize() > maximoBytes) {
            throw new IllegalArgumentException("La imagen no debe pesar más de 5MB.");
        }

        String nombreOriginal = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename();
        String extension = "";
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto >= 0) {
            extension = nombreOriginal.substring(punto + 1).toLowerCase();
        }

        Set<String> permitidas = Set.of("jpg", "jpeg", "png", "webp", "gif");
        if (!permitidas.contains(extension)) {
            throw new IllegalArgumentException("Formato de imagen inválido. Usa JPG, JPEG, PNG, WEBP o GIF.");
        }

        Path carpeta = Path.of("uploads", "productos").toAbsolutePath().normalize();
        Files.createDirectories(carpeta);

        String nombreSeguro = UUID.randomUUID() + "." + extension;
        Path destino = carpeta.resolve(nombreSeguro).normalize();
        archivo.transferTo(destino.toFile());

        return "uploads/productos/" + nombreSeguro;
    }

    @PostMapping("/productos/estado/{id}")
    public String cambiarEstadoProducto(@PathVariable Long id) {
        Producto p = productoRepository.findById(id).orElseThrow();
        p.setActivo(!p.isActivo());
        productoRepository.save(p);
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirect) {
        Producto p = productoRepository.findById(id).orElseThrow();
        if (detalleVentaRepository.existsByProducto(p)) {
            p.setActivo(false);
            productoRepository.save(p);
            redirect.addFlashAttribute("error", "El producto tiene ventas asociadas. Se desactivó en lugar de eliminarse.");
        } else {
            productoRepository.delete(p);
            redirect.addFlashAttribute("exito", "Producto eliminado correctamente.");
        }
        return "redirect:/admin/productos";
    }

    @GetMapping("/categorias")
    public String categorias(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("categoria", new Categoria());
        return "admin/categorias";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@ModelAttribute Categoria categoria, RedirectAttributes redirect) {
        if (categoria.getId() == null && categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
            redirect.addFlashAttribute("error", "Ya existe una categoría con ese nombre.");
        } else {
            if (categoria.getId() == null) categoria.setActivo(true);
            categoriaRepository.save(categoria);
            redirect.addFlashAttribute("exito", "Categoría guardada correctamente.");
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/categorias/estado/{id}")
    public String cambiarEstadoCategoria(@PathVariable Long id) {
        Categoria c = categoriaRepository.findById(id).orElseThrow();
        c.setActivo(!c.isActivo());
        categoriaRepository.save(c);
        return "redirect:/admin/categorias";
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        var clientes = usuarioRepository.findAll().stream().filter(u -> u.getRol() == Rol.CLIENTE).toList();
        model.addAttribute("clientes", clientes);
        model.addAttribute("repo", usuarioRepository);
        return "admin/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Usuario cliente = usuarioRepository.findById(id).orElseThrow();
        if (cliente.getRol() != Rol.CLIENTE) {
            redirect.addFlashAttribute("error", "Solo se pueden editar clientes.");
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", cliente);
        return "admin/cliente-form";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute("cliente") Usuario clienteForm, RedirectAttributes redirect) {
        try {
            Usuario cliente = usuarioRepository.findById(clienteForm.getId()).orElseThrow();
            if (cliente.getRol() != Rol.CLIENTE) {
                throw new IllegalArgumentException("Solo se pueden editar clientes.");
            }

            limpiarYValidarCliente(clienteForm, cliente.getId());

            cliente.setNombres(clienteForm.getNombres());
            cliente.setApellidos(clienteForm.getApellidos());
            cliente.setCorreo(clienteForm.getCorreo());
            cliente.setTelefono(clienteForm.getTelefono());
            cliente.setActivo(clienteForm.isActivo());
            usuarioRepository.save(cliente);

            redirect.addFlashAttribute("exito", "Cliente actualizado correctamente.");
            return "redirect:/admin/clientes";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clientes/editar/" + clienteForm.getId();
        }
    }

    private void limpiarYValidarCliente(Usuario cliente, Long idActual) {
        String nombres = limpiarTexto(cliente.getNombres());
        String apellidos = limpiarTexto(cliente.getApellidos());
        String correo = cliente.getCorreo() == null ? "" : cliente.getCorreo().trim().toLowerCase();
        String telefono = cliente.getTelefono() == null ? "" : cliente.getTelefono().trim();

        if (nombres.length() < 2 || nombres.length() > 80) {
            throw new IllegalArgumentException("Los nombres deben tener entre 2 y 80 caracteres.");
        }
        if (apellidos.length() < 2 || apellidos.length() > 80) {
            throw new IllegalArgumentException("Los apellidos deben tener entre 2 y 80 caracteres.");
        }
        if (!nombres.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            throw new IllegalArgumentException("Los nombres no deben contener números ni símbolos.");
        }
        if (!apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            throw new IllegalArgumentException("Los apellidos no deben contener números ni símbolos.");
        }
        if (!correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Ingresa un correo válido.");
        }
        if (!telefono.matches("^9\\d{8}$")) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos y empezar con 9.");
        }

        Optional<Usuario> existente = usuarioRepository.findByCorreo(correo);
        if (existente.isPresent() && !existente.get().getId().equals(idActual)) {
            throw new IllegalArgumentException("Ya existe otro usuario con ese correo.");
        }

        cliente.setNombres(nombres);
        cliente.setApellidos(apellidos);
        cliente.setCorreo(correo);
        cliente.setTelefono(telefono);
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
    }

    @GetMapping("/productos/{id}/movimientos")
    public String movimientosProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id).orElseThrow();
        model.addAttribute("producto", producto);
        model.addAttribute("movimientos", movimientoRepository.findByProductoIdOrderByFechaDesc(id));
        return "admin/producto-movimientos";
    }

    @GetMapping("/ventas")
    public String ventas(Model model) {
        model.addAttribute("ventas", ventaRepository.findAll());
        return "admin/ventas";
    }

    @GetMapping("/ventas/{id}")
    public String ventaDetalle(@PathVariable Long id, Model model) {
        // Cargar la venta con sus detalles y productos para evitar LazyInitializationException en Thymeleaf.
        model.addAttribute("venta", ventaRepository.findWithDetallesById(id).orElseThrow());
        return "admin/venta-detalle";
    }

    @GetMapping("/ventas/{id}/captura")
    public ResponseEntity<?> verCapturaPago(@PathVariable Long id) throws Exception {
        Venta venta = ventaRepository.findById(id).orElseThrow();
        String rutaGuardada = venta.getCapturaPago();

        if (rutaGuardada == null || rutaGuardada.isBlank()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<h3>Esta venta no tiene captura registrada.</h3><a href='/admin/ventas/" + id + "'>Volver</a>");
        }

        Optional<Path> archivo = buscarArchivoCaptura(rutaGuardada);
        if (archivo.isEmpty()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<h3>No se encontró el archivo de la captura.</h3>"
                            + "<p>La venta sí tiene registrada la ruta: <b>" + rutaGuardada + "</b></p>"
                            + "<p>Esto pasa cuando cambiaste de carpeta/proyecto y el archivo físico quedó en el proyecto anterior.</p>"
                            + "<p>Las nuevas capturas se guardarán en <b>uploads/pagos</b> del proyecto actual.</p>"
                            + "<a href='/admin/ventas/" + id + "'>Volver al detalle</a>");
        }

        Path path = archivo.get();
        Resource recurso = new PathResource(path);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                .body(recurso);
    }

    private Optional<Path> buscarArchivoCaptura(String rutaGuardada) throws Exception {
        String limpia = rutaGuardada.trim().replace("\\", "/");
        while (limpia.startsWith("/")) {
            limpia = limpia.substring(1);
        }

        String nombreArchivo = Path.of(limpia).getFileName().toString();
        List<Path> candidatos = new ArrayList<>();

        candidatos.add(Path.of(limpia));
        candidatos.add(Path.of("uploads").resolve(limpia));
        candidatos.add(Path.of("uploads", "pagos", nombreArchivo));
        candidatos.add(Path.of("src", "main", "resources", "static").resolve(limpia));
        candidatos.add(Path.of("src", "main", "resources", "static", "pagos", nombreArchivo));
        candidatos.add(Path.of("target", "classes", "static").resolve(limpia));
        candidatos.add(Path.of("target", "classes", "static", "pagos", nombreArchivo));

        for (Path candidato : candidatos) {
            Path normalizado = candidato.toAbsolutePath().normalize();
            if (Files.exists(normalizado) && Files.isRegularFile(normalizado)) {
                return Optional.of(normalizado);
            }
        }

        // Fallback: busca el archivo por nombre en la carpeta actual y hasta en la carpeta Downloads.
        // Sirve cuando se cambió de ZIP/carpeta y la BD quedó apuntando a una captura subida en una versión anterior.
        Path base = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> raices = new ArrayList<>();
        raices.add(base);
        if (base.getParent() != null) raices.add(base.getParent());
        if (base.getParent() != null && base.getParent().getParent() != null) raices.add(base.getParent().getParent());

        for (Path raiz : raices) {
            if (!Files.exists(raiz)) continue;
            try (var stream = Files.find(raiz, 6, (p, attrs) -> attrs.isRegularFile() && p.getFileName().toString().equals(nombreArchivo))) {
                Optional<Path> encontrado = stream.findFirst();
                if (encontrado.isPresent()) {
                    return encontrado.map(p -> p.toAbsolutePath().normalize());
                }
            } catch (Exception ignored) {
                // Ignorar carpetas sin permiso o rutas demasiado grandes.
            }
        }

        return Optional.empty();
    }

    @PostMapping("/ventas/aprobar/{id}")
    public String aprobarVenta(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try { ventaService.aprobar(id, principal.getName()); redirect.addFlashAttribute("exito", "Venta aprobada y stock descontado."); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/ventas/" + id;
    }

    @PostMapping("/ventas/rechazar/{id}")
    public String rechazarVenta(@PathVariable Long id, @RequestParam String observacion, Principal principal, RedirectAttributes redirect) {
        try { ventaService.rechazar(id, observacion, principal.getName()); redirect.addFlashAttribute("exito", "Venta rechazada."); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/ventas/" + id;
    }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("movimientos", movimientoRepository.findAllByOrderByFechaDesc());
        return "admin/inventario";
    }

    @PostMapping("/inventario/agregar")
    public String agregarStock(@RequestParam Long productoId, @RequestParam int cantidad, @RequestParam String motivo, Principal principal, RedirectAttributes redirect) {
        try {
            if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            Producto p = productoRepository.findById(productoId).orElseThrow();
            int antes = p.getStock();
            p.setStock(antes + cantidad);
            productoRepository.save(p);
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(p);
            mov.setTipo("ENTRADA");
            mov.setCantidad(cantidad);
            mov.setStockAntes(antes);
            mov.setStockDespues(p.getStock());
            mov.setMotivo(motivo);
            mov.setUsuario(principal.getName());
            movimientoRepository.save(mov);
            redirect.addFlashAttribute("exito", "Stock agregado correctamente.");
        } catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/inventario";
    }

    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        model.addAttribute("config", configuracionRepository.findById(1L).orElseGet(() -> configuracionRepository.save(new ConfiguracionTienda())));
        return "admin/configuracion";
    }

    @PostMapping("/configuracion")
    public String guardarConfiguracion(@ModelAttribute ConfiguracionTienda config, RedirectAttributes redirect) {
        config.setId(1L);
        configuracionRepository.save(config);
        redirect.addFlashAttribute("exito", "Configuración guardada.");
        return "redirect:/admin/configuracion";
    }
}
