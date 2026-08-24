-- Ejecutar una sola vez en phpMyAdmin o MySQL Workbench.
CREATE DATABASE IF NOT EXISTS bd_formen
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bd_formen;

-- Las tablas se crean y actualizan automáticamente con JPA/Hibernate
-- porque application.properties usa: spring.jpa.hibernate.ddl-auto=update
-- Los seis usuarios del equipo se insertan desde DataInitializer.java
-- y sus contraseñas se almacenan cifradas con BCrypt.
