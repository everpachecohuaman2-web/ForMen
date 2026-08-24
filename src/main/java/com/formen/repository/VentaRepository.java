package com.formen.repository;

import com.formen.entity.EstadoVenta;
import com.formen.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    boolean existsByCodigoOperacion(String codigoOperacion);
    Optional<Venta> findTopByOrderByIdDesc();
    List<Venta> findTop10ByOrderByFechaVentaDesc();
    List<Venta> findAllByOrderByFechaVentaDesc();

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    Optional<Venta> findWithDetallesById(Long id);
    List<Venta> findByEstadoOrderByFechaVentaDesc(EstadoVenta estado);
    long countByEstado(EstadoVenta estado);

    @Query("select coalesce(sum(v.total),0) from Venta v where v.estado = com.formen.entity.EstadoVenta.APROBADA")
    BigDecimal totalVentasAprobadas();
}
