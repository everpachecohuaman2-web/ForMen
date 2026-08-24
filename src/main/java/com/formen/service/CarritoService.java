package com.formen.service;

import com.formen.entity.Producto;
import com.formen.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {
    private final ProductoRepository productoRepository;
    public CarritoService(ProductoRepository productoRepository) { this.productoRepository = productoRepository; }

    @SuppressWarnings("unchecked")
    public List<ItemCarrito> obtener(HttpSession session) {
        Object obj = session.getAttribute("carrito");
        if (obj instanceof List<?>) return (List<ItemCarrito>) obj;
        List<ItemCarrito> nuevo = new ArrayList<>();
        session.setAttribute("carrito", nuevo);
        return nuevo;
    }

    public void agregar(HttpSession session, Long productoId) {
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        if (!producto.isActivo() || !producto.getCategoria().isActivo() || producto.getStock() <= 0) {
            throw new IllegalArgumentException("Producto no disponible.");
        }
        List<ItemCarrito> items = obtener(session);
        Optional<ItemCarrito> existente = items.stream().filter(i -> i.getProducto().getId().equals(productoId)).findFirst();
        if (existente.isPresent()) {
            int nuevaCantidad = existente.get().getCantidad() + 1;
            if (nuevaCantidad > producto.getStock()) throw new IllegalArgumentException("Stock insuficiente.");
            existente.get().setCantidad(nuevaCantidad);
        } else {
            items.add(new ItemCarrito(producto, 1));
        }
    }

    public void actualizar(HttpSession session, Long productoId, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        Producto producto = productoRepository.findById(productoId).orElseThrow();
        if (cantidad > producto.getStock()) throw new IllegalArgumentException("Stock insuficiente.");
        obtener(session).stream().filter(i -> i.getProducto().getId().equals(productoId)).findFirst().ifPresent(i -> i.setCantidad(cantidad));
    }

    public void eliminar(HttpSession session, Long productoId) { obtener(session).removeIf(i -> i.getProducto().getId().equals(productoId)); }
    public void vaciar(HttpSession session) { obtener(session).clear(); }
    public BigDecimal total(HttpSession session) { return obtener(session).stream().map(ItemCarrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
