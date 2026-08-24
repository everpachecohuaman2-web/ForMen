package com.formen.repository;

import com.formen.entity.Rol;
import com.formen.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    long countByRol(Rol rol);

    @Query("select coalesce(sum(v.total),0) from Venta v where v.cliente.id = :clienteId and v.estado = com.formen.entity.EstadoVenta.APROBADA")
    BigDecimal totalGastado(Long clienteId);

    @Query("select count(v) from Venta v where v.cliente.id = :clienteId")
    long comprasRealizadas(Long clienteId);
}
