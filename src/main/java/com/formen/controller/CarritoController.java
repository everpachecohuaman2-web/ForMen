package com.formen.controller;

import com.formen.entity.MetodoPago;
import com.formen.service.CarritoService;
import com.formen.service.TiendaService;
import com.formen.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/carrito")
public class CarritoController {
    private final CarritoService carritoService;
    private final VentaService ventaService;
    private final TiendaService tiendaService;

    public CarritoController(CarritoService carritoService, VentaService ventaService, TiendaService tiendaService) {
        this.carritoService = carritoService;
        this.ventaService = ventaService;
        this.tiendaService = tiendaService;
    }

    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        model.addAttribute("items", carritoService.obtener(session));
        model.addAttribute("total", carritoService.total(session));
        return "carrito";
    }

    @GetMapping("/agregar")
    public String agregar(@RequestParam Long id, HttpSession session, RedirectAttributes redirect) {
        try { carritoService.agregar(session, id); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/carrito";
    }

    @GetMapping("/eliminar")
    public String eliminar(@RequestParam Long id, HttpSession session) {
        carritoService.eliminar(session, id);
        return "redirect:/carrito";
    }

    @GetMapping("/actualizar")
    public String actualizarCantidad(@RequestParam Long id, @RequestParam int cantidad, HttpSession session, RedirectAttributes redirect) {
        try { carritoService.actualizar(session, id, cantidad); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/carrito";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, Principal principal) {
        if (carritoService.obtener(session).isEmpty()) return "redirect:/carrito";
        if (principal == null) {
            session.setAttribute("REDIRECT_AFTER_LOGIN", "/carrito/checkout");
            return "redirect:/registro?redirect=checkout";
        }
        model.addAttribute("items", carritoService.obtener(session));
        model.addAttribute("total", carritoService.total(session));
        model.addAttribute("config", tiendaService.config());
        model.addAttribute("metodos", MetodoPago.values());
        return "checkout";
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam MetodoPago metodoPago,
                            @RequestParam String codigoOperacion,
                            @RequestParam MultipartFile capturaPago,
                            HttpSession session,
                            Principal principal,
                            RedirectAttributes redirect) {
        try {
            var venta = ventaService.crearVenta(session, principal, metodoPago, codigoOperacion, capturaPago);
            redirect.addFlashAttribute("exito", "Compra registrada. Tu venta " + venta.getNumero() + " quedó pendiente de aprobación.");
            return "redirect:/carrito";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito/checkout";
        }
    }
}
