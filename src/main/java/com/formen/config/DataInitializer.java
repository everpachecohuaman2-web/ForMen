package com.formen.config;

import com.formen.entity.Categoria;
import com.formen.entity.ConfiguracionTienda;
import com.formen.entity.Producto;
import com.formen.entity.Rol;
import com.formen.entity.Usuario;
import com.formen.repository.CategoriaRepository;
import com.formen.repository.ConfiguracionTiendaRepository;
import com.formen.repository.ProductoRepository;
import com.formen.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ConfiguracionTiendaRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           ConfiguracionTiendaRepository configuracionRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.configuracionRepository = configuracionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuariosDelEquipo();

        if (!configuracionRepository.existsById(1L)) {
            configuracionRepository.save(new ConfiguracionTienda());
        }

        Map<String, String> categorias = new LinkedHashMap<>();
        categorias.put("Formal", "Prendas elegantes para eventos y oficina");
        categorias.put("Casual", "Moda masculina para el día a día");
        categorias.put("Deportiva", "Ropa cómoda para entrenamiento");
        categorias.put("Accesorios", "Accesorios masculinos");
        categorias.put("Polos", "Polos para hombre");
        categorias.put("Camisas", "Camisas para hombre");
        categorias.put("Pantalones", "Pantalones masculinos");
        categorias.put("Jeans", "Jeans para hombre");
        categorias.put("Casacas", "Casacas masculinas");
        categorias.put("Zapatillas", "Zapatillas para hombre");
        categorias.put("Correas", "Correas masculinas");
        categorias.put("Gorras", "Gorras para hombre");
        categorias.put("Relojes", "Relojes masculinos");

        categorias.forEach((nombre, descripcion) ->
            categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nombre, descripcion)))
        );

        if (productoRepository.count() == 0) {
            crearProducto("Saco Clásico Premium", "Elegancia y estilo en cada detalle.", "189.00", "saco_premium.jpg", "Formal", 15, 3);
            crearProducto("Polo Casual", "Comodidad para el día a día.", "79.00", "polo_casual.jpg", "Casual", 30, 5);
            crearProducto("Camisa Elegante", "Ideal para eventos y reuniones.", "80.00", "Camisa.jpeg", "Formal", 25, 5);
            crearProducto("Cinturón de Cuero", "Durabilidad y estilo.", "39.90", "correa.webp", "Accesorios", 40, 8);
            crearProducto("Reloj Elegante", "Organiza tu día con estilo.", "139.00", "reloj.webp", "Accesorios", 20, 3);
            crearProducto("Pantalón Chino", "Versátil y moderno.", "129.00", "Pantalon.jpeg", "Casual", 18, 4);
            crearProducto("Zapatillas Urbanas", "Comodidad para tu día a día.", "189.00", "zapatillas.png", "Casual", 16, 4);
            crearProducto("Casaca Moderna", "Ideal para clima frío.", "159.00", "Casaca.png", "Casual", 12, 2);
            crearProducto("Lentes de Sol", "Protección y estilo en uno.", "79.00", "lentes.jpg", "Accesorios", 22, 5);
            crearProducto("Conjunto Deportivo", "Ideal para entrenamientos.", "150.00", "depor1.webp", "Deportiva", 20, 4);
            crearProducto("Short Deportivo", "Ligero y cómodo.", "70.00", "depor2.webp", "Deportiva", 25, 5);
            crearProducto("Casaca Deportiva", "Perfecto para actividad física.", "85.00", "depor3.webp", "Deportiva", 13, 3);
        }
    }

    /**
     * Crea los usuarios académicos solicitados. Las contraseñas se guardan
     * con BCrypt y solo se crean cuando el correo todavía no existe.
     */
    private void crearUsuariosDelEquipo() {
        crearUsuarioSistema(
            "Jorge Junior", "Bazán Carrión", "bazan@formen.com", "900000001",
            "bazan123", Rol.REPORTES
        );
        crearUsuarioSistema(
            "Kebin Augusto", "García Calvay", "garcia@formen.com", "900000002",
            "garcia123", Rol.PRODUCTOS
        );
        crearUsuarioSistema(
            "Antony Oswer", "Nuñez Cruz", "nunez@formen.com", "900000003",
            "nunez123", Rol.INVENTARIO
        );
        crearUsuarioSistema(
            "Gabriel", "Orellano Sánchez", "orellano@formen.com", "900000004",
            "orellano123", Rol.CLIENTES
        );
        crearUsuarioSistema(
            "José", "Rumiche Saavedra", "rumiche@formen.com", "900000005",
            "rumiche123", Rol.ADMIN
        );
        crearUsuarioSistema(
            "Everth Jhonatan", "Pacheco Huamán", "pacheco@formen.com", "900000006",
            "pacheco123", Rol.VENTAS
        );
    }

    private void crearUsuarioSistema(String nombres,
                                     String apellidos,
                                     String correo,
                                     String telefono,
                                     String passwordPlano,
                                     Rol rol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setOrigenRegistro("SISTEMA");
        usuarioRepository.save(usuario);

        System.out.printf("Usuario creado: %s | rol: %s%n", correo, rol);
    }

    private void crearProducto(String nombre,
                               String descripcion,
                               String precio,
                               String imagen,
                               String categoriaNombre,
                               int stock,
                               int stockMinimo) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(categoriaNombre).orElseThrow();
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(new BigDecimal(precio));
        producto.setImagen(imagen);
        producto.setCategoria(categoria);
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setActivo(true);
        productoRepository.save(producto);
    }
}
