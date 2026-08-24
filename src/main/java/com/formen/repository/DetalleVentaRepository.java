package com.formen.repository;

import com.formen.entity.DetalleVenta;
import com.formen.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    boolean existsByProducto(Producto producto);
}
