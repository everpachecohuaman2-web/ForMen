# Guía breve de seguridad y roles

## Archivos importantes

### `entity/Rol.java`
Contiene los nombres de los roles disponibles.

### `entity/Usuario.java`
Representa la tabla `usuarios`. Guarda nombres, apellidos, correo, contraseña cifrada, rol, estado y fecha de registro.

### `security/CustomUserDetailsService.java`
Busca el correo en la base de datos y entrega a Spring Security la contraseña cifrada y el rol.

### `config/SecurityConfig.java`
Define:

- páginas públicas;
- páginas que requieren sesión;
- permisos de cada rol;
- formulario de login;
- cierre de sesión;
- página de acceso denegado;
- redirección según rol;
- BCrypt y CSRF.

### `config/DataInitializer.java`
Crea los seis usuarios iniciales del equipo. Antes de guardar una contraseña usa `passwordEncoder.encode(...)`.

### `templates/fragments.html`
Oculta o muestra las opciones del menú con `sec:authorize`. Esto mejora la interfaz, pero la verdadera protección sigue estando en `SecurityConfig.java`.

## Diferencia entre autenticación y autorización

- **Autenticación:** comprobar quién es el usuario mediante correo y contraseña.
- **Autorización:** comprobar qué módulos puede usar según su rol.

## Ejemplo de seguridad

El rol REPORTES puede realizar un GET para consultar una venta, pero no puede realizar el POST de aprobación:

```java
.requestMatchers(HttpMethod.POST,
    "/admin/ventas/aprobar/**",
    "/admin/ventas/rechazar/**")
.hasAnyRole("ADMIN", "VENTAS")
```

Aunque el usuario intente escribir esa dirección manualmente, el servidor devuelve acceso denegado.
