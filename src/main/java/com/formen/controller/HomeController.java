package com.formen.controller;

import com.formen.entity.Producto;
import com.formen.repository.CategoriaRepository;
import com.formen.service.TiendaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {
    private final TiendaService tiendaService;
    private final CategoriaRepository categoriaRepository;

    public HomeController(TiendaService tiendaService, CategoriaRepository categoriaRepository) {
        this.tiendaService = tiendaService;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String buscar,
                        @RequestParam(required = false) BigDecimal precioMin,
                        @RequestParam(required = false) BigDecimal precioMax,
                        @RequestParam(required = false) String talla) {

        boolean filtroInvalido = false;

        if (precioMin != null && precioMin.compareTo(BigDecimal.ZERO) < 0) {
            precioMin = null;
            filtroInvalido = true;
        }

        if (precioMax != null && precioMax.compareTo(BigDecimal.ZERO) < 0) {
            precioMax = null;
            filtroInvalido = true;
        }

        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            filtroInvalido = true;
            BigDecimal temporal = precioMin;
            precioMin = precioMax;
            precioMax = temporal;
        }

        List<Producto> productos = tiendaService.filtrar(buscar, precioMin, precioMax);
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("config", tiendaService.config());

        // Mantiene los filtros escritos para que no se borren al presionar buscar.
        model.addAttribute("buscar", buscar);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("talla", talla);

        if (filtroInvalido) {
            model.addAttribute("error", "Los filtros de precio no aceptan valores negativos. Si el mínimo es mayor que el máximo, se corrige automáticamente.");
        }

        return "index";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) { model.addAttribute("config", tiendaService.config()); return "nosotros"; }

    @GetMapping("/contacto")
    public String contacto(Model model) { model.addAttribute("config", tiendaService.config()); return "contacto"; }

    @GetMapping("/contacto/enviar")
    public String contactoEnviar(Model model) {
        model.addAttribute("exito", "Mensaje enviado correctamente.");
        model.addAttribute("config", tiendaService.config());
        return "contacto";
    }
}
