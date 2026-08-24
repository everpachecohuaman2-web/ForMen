package com.formen.controller;

import com.formen.entity.Categoria;
import com.formen.entity.Producto;
import com.formen.repository.CategoriaRepository;
import com.formen.repository.ProductoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/colecciones")
public class ProductoController {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String colecciones(Model model) {
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("productos", productoRepository.findByActivoTrueAndCategoriaActivoTrueOrderByIdAsc());
        model.addAttribute("formal", categoria("Formal"));
        model.addAttribute("casual", categoria("Casual"));
        model.addAttribute("accesorios", categoria("Accesorios"));
        model.addAttribute("deportiva", categoria("Deportiva"));
        return "colecciones";
    }

    private java.util.List<Producto> categoria(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .map(productoRepository::findByCategoriaAndActivoTrue)
                .orElse(java.util.Collections.emptyList());
    }

    @GetMapping("/detalle/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id).orElse(null);
        model.addAttribute("producto", producto);
        return "detalle";
    }
}
