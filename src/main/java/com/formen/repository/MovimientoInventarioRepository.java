package com.formen.repository;

import com.formen.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findAllByOrderByFechaDesc();
    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId);
}
