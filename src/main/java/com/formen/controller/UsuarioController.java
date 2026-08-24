package com.formen.controller;

import com.formen.entity.Rol;
import com.formen.entity.Usuario;
import com.formen.repository.UsuarioRepository;
import com.formen.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                             CustomUserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String redirect, HttpSession session) {
        if ("checkout".equalsIgnoreCase(redirect)) {
            session.setAttribute("REDIRECT_AFTER_LOGIN", "/carrito/checkout");
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(@RequestParam(required = false) String redirect, Model model, HttpSession session) {
        if ("checkout".equalsIgnoreCase(redirect) || "true".equalsIgnoreCase(redirect)) {
            session.setAttribute("REDIRECT_AFTER_LOGIN", "/carrito/checkout");
        }
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registroSubmit(@Valid @ModelAttribute("usuario") Usuario usuario,
                                 BindingResult result,
                                 @RequestParam String password,
                                 HttpServletRequest request,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirect) {
        limpiarDatos(usuario);

        if (password == null || password.trim().length() < 6) {
            result.rejectValue("password", "password.min", "La contraseña debe tener mínimo 6 caracteres.");
        }

        if (usuario.getCorreo() != null && usuarioRepository.existsByCorreo(usuario.getCorreo().trim().toLowerCase())) {
            result.rejectValue("correo", "correo.duplicado", "Ya existe una cuenta con ese correo.");
        }

        if (result.hasErrors()) {
            model.addAttribute("error", "Corrige los datos marcados antes de continuar.");
            return "registro";
        }

        usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());
        usuario.setRol(Rol.CLIENTE);
        usuario.setOrigenRegistro("WEB");
        usuario.setActivo(true);
        usuario.setPassword(passwordEncoder.encode(password.trim()));
        usuarioRepository.save(usuario);

        iniciarSesion(usuario.getCorreo(), request);

        Object destino = session.getAttribute("REDIRECT_AFTER_LOGIN");
        session.removeAttribute("REDIRECT_AFTER_LOGIN");
        redirect.addFlashAttribute("exito", "Cuenta creada correctamente. Ya puedes finalizar tu compra.");
        if (destino != null && destino.toString().startsWith("/")) {
            return "redirect:" + destino;
        }
        return "redirect:/";
    }

    private void limpiarDatos(Usuario usuario) {
        if (usuario.getNombres() != null) usuario.setNombres(usuario.getNombres().trim().replaceAll("\\s+", " "));
        if (usuario.getApellidos() != null) usuario.setApellidos(usuario.getApellidos().trim().replaceAll("\\s+", " "));
        if (usuario.getCorreo() != null) usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());
        if (usuario.getTelefono() != null) usuario.setTelefono(usuario.getTelefono().trim());
    }

    private void iniciarSesion(String correo, HttpServletRequest request) {
        var userDetails = userDetailsService.loadUserByUsername(correo);
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
