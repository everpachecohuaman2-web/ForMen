package com.formen.config;

import com.formen.entity.Rol;
import com.formen.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Frontend público de la tienda.
                .requestMatchers(
                    "/", "/colecciones/**", "/nosotros", "/contacto/**",
                    "/login", "/registro", "/acceso-denegado",
                    "/imagenes/**", "/logo/**", "/uploads/**", "/css/**", "/js/**",
                    "/api/productos"
                ).permitAll()

                // Dashboard disponible para todos los colaboradores autenticados.
                .requestMatchers("/admin", "/admin/", "/admin/dashboard")
                    .hasAnyRole("ADMIN", "REPORTES", "PRODUCTOS", "INVENTARIO", "CLIENTES", "VENTAS", "EMPLEADO")

                // Reportes: acceso de solo lectura para el rol REPORTES.
                .requestMatchers("/admin/reportes/**")
                    .hasAnyRole("ADMIN", "REPORTES")

                // Gestión de productos y categorías.
                .requestMatchers("/admin/productos/**", "/admin/categorias/**")
                    .hasAnyRole("ADMIN", "PRODUCTOS")

                // Gestión del inventario.
                .requestMatchers("/admin/inventario/**")
                    .hasAnyRole("ADMIN", "INVENTARIO")

                // Gestión de clientes.
                .requestMatchers("/admin/clientes/**")
                    .hasAnyRole("ADMIN", "CLIENTES")

                // Solo ADMIN y VENTAS pueden aprobar o rechazar pagos.
                .requestMatchers(HttpMethod.POST, "/admin/ventas/aprobar/**", "/admin/ventas/rechazar/**")
                    .hasAnyRole("ADMIN", "VENTAS")

                // REPORTES puede consultar ventas, pero no modificarlas.
                .requestMatchers(HttpMethod.GET, "/admin/ventas/**")
                    .hasAnyRole("ADMIN", "VENTAS", "REPORTES")

                // Configuración y listado de usuarios del sistema: solo administrador.
                .requestMatchers("/admin/configuracion/**", "/admin/usuarios-sistema/**")
                    .hasRole("ADMIN")

                .requestMatchers("/pagos/**")
                    .hasAnyRole("ADMIN", "VENTAS")

                // Para confirmar una compra basta con tener una sesión válida.
                .requestMatchers("/carrito/confirmar")
                    .authenticated()
                .requestMatchers("/carrito/**")
                    .permitAll()

                // Cualquier otra ruta administrativa queda cerrada por defecto.
                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                .anyRequest()
                    .permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/acceso-denegado")
            );

        // CSRF queda habilitado. Thymeleaf agrega automáticamente el token
        // a los formularios POST que usan th:action.
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String correo = authentication.getName();

            usuarioRepository.findByCorreo(correo).ifPresentOrElse(usuario -> {
                try {
                    Object destino = request.getSession().getAttribute("REDIRECT_AFTER_LOGIN");
                    request.getSession().removeAttribute("REDIRECT_AFTER_LOGIN");

                    // Si inició sesión desde el checkout, vuelve a terminar su compra.
                    if (destino != null && destino.toString().startsWith("/carrito/")) {
                        response.sendRedirect(destino.toString());
                        return;
                    }

                    response.sendRedirect(rutaInicialPorRol(usuario.getRol()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, () -> {
                try {
                    response.sendRedirect("/");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }

    private String rutaInicialPorRol(Rol rol) {
        switch (rol) {
            case REPORTES:
                return "/admin/reportes";
            case PRODUCTOS:
                return "/admin/productos";
            case INVENTARIO:
                return "/admin/inventario";
            case CLIENTES:
                return "/admin/clientes";
            case VENTAS:
                return "/admin/ventas";
            case ADMIN:
            case EMPLEADO:
                return "/admin/dashboard";
            case CLIENTE:
            default:
                return "/";
        }
    }
}
