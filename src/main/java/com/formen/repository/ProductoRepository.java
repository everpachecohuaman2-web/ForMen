package com.formen.repository;

import com.formen.entity.Categoria;
import com.formen.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrueOrderByIdAsc();
    List<Producto> findByActivoTrueAndCategoriaActivoTrueOrderByIdAsc();
    List<Producto> findByActivoTrueAndCategoriaActivoTrueAndStockGreaterThanOrderByIdAsc(Integer stock);
    List<Producto> findByCategoriaAndActivoTrue(Categoria categoria);
    boolean existsByNombreIgnoreCaseAndCategoria(String nombre, Categoria categoria);
    long countByActivoTrue();
    long countByStockLessThanEqual(Integer stock);
}
