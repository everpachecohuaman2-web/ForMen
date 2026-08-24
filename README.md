# ForMen — Frontend, Backend, Base de Datos y Seguridad

Proyecto de comercio electrónico masculino desarrollado con una arquitectura MVC.

## Tecnologías

- **Frontend:** HTML, Bootstrap 5, Thymeleaf y Thymeleaf Extras Spring Security.
- **Backend:** Java 21, Spring Boot, Spring MVC, controladores, servicios y repositorios.
- **Base de datos:** MySQL/XAMPP, Spring Data JPA e Hibernate.
- **Seguridad:** Spring Security, login por correo, BCrypt, CSRF, control de sesiones y autorización por roles.

## 1. Crear la base de datos

En phpMyAdmin ejecuta:

```sql
CREATE DATABASE bd_formen CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

También puedes importar `database/crear_bd_formen.sql`.

No es necesario crear las tablas manualmente. Hibernate las crea al iniciar el proyecto porque se usa:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## 2. Ejecutar

1. Inicia **Apache** y **MySQL** en XAMPP.
2. Abre la carpeta `ForMen_Rehecho` en IntelliJ IDEA.
3. Espera a que Maven descargue las dependencias.
4. Ejecuta `ForMenApplication.java`.
5. Abre `http://localhost:8080`.

## 3. Usuarios del equipo

Los usuarios se crean automáticamente la primera vez mediante `DataInitializer.java`.

| Integrante | Correo | Contraseña | Rol | Módulo principal |
|---|---|---|---|---|
| Bazán Carrión Jorge Junior | `bazan@formen.com` | `bazan123` | REPORTES | Reportes de solo lectura |
| García Calvay Kebin Augusto | `garcia@formen.com` | `garcia123` | PRODUCTOS | Productos y categorías |
| Nuñez Cruz Antony Oswer | `nunez@formen.com` | `nunez123` | INVENTARIO | Stock y movimientos |
| Orellano Sánchez Gabriel | `orellano@formen.com` | `orellano123` | CLIENTES | Gestión de clientes |
| Rumiche Saavedra José | `rumiche@formen.com` | `rumiche123` | ADMIN | Acceso total |
| Pacheco Huamán Everth Jhonatan | `pacheco@formen.com` | `pacheco123` | VENTAS | Revisar, aprobar y rechazar ventas |

> Las contraseñas no se guardan como texto plano en MySQL. Se convierten en hashes BCrypt.

## 4. Permisos

- **ADMIN:** todos los módulos, usuarios del sistema y configuración.
- **REPORTES:** dashboard, reporte general y detalle de ventas; no puede aprobar ni modificar.
- **PRODUCTOS:** productos, imágenes, categorías y movimientos del producto.
- **INVENTARIO:** stock, entradas y movimientos de inventario.
- **CLIENTES:** listado y edición de clientes.
- **VENTAS:** listado, detalle, comprobantes, aprobación y rechazo de ventas.
- **CLIENTE:** tienda, carrito y compras.

La seguridad no depende únicamente de ocultar botones. `SecurityConfig.java` protege las rutas del servidor; si un usuario escribe una URL de otro módulo, Spring Security lo envía a `/acceso-denegado`.

## 5. Flujo del login

1. El formulario envía correo y contraseña a `/login`.
2. `CustomUserDetailsService` busca al usuario en MySQL.
3. Spring Security compara la contraseña con BCrypt.
4. Se crea la sesión autenticada.
5. `AuthenticationSuccessHandler` redirige al módulo correspondiente al rol.

## 6. Explicación sencilla para Bazán

Bazán tiene el rol **REPORTES**, que es intencionalmente fácil de exponer:

- Su cuenta se crea en `DataInitializer.java`.
- Su rol se define con `Rol.REPORTES`.
- En `SecurityConfig.java`, ese rol puede entrar a `/admin/reportes` y consultar `/admin/ventas/**` mediante GET.
- No puede enviar los POST de aprobar o rechazar ventas.
- En `reportes.html` solo se muestran datos obtenidos desde los repositorios.

Una forma simple de explicarlo es: **“Mi usuario consulta información, pero Spring Security bloquea cualquier operación que modifique ventas.”**


## Solución del error `Unable to determine Dialect`

El proyecto ahora usa una URL JDBC con `createDatabaseIfNotExist=true` y define explícitamente `org.hibernate.dialect.MySQLDialect`. Antes de ejecutar, inicia **MySQL en XAMPP**. La configuración predeterminada es `root` sin contraseña, puerto `3306`.

Si tu MySQL usa contraseña o un puerto distinto, modifica `src/main/resources/application.properties`. Se recomienda ejecutar con **JDK 21**; el aviso `java.lang.System::load` mostrado por Java 25 es una advertencia y no es el error de conexión.
