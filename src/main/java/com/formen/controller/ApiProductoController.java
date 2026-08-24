package com.formen.controller;

import com.formen.entity.Producto;
import com.formen.repository.ProductoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ApiProductoController {
    private final ProductoRepository productoRepository;
    public ApiProductoController(ProductoRepository productoRepository) { this.productoRepository = productoRepository; }
    @GetMapping
    public List<Producto> listarProductos() { return productoRepository.findByActivoTrueAndCategoriaActivoTrueOrderByIdAsc(); }
}
