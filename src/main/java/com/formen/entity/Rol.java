package com.formen.entity;

/**
 * Roles disponibles en el sistema ForMen.
 * Cada rol representa un módulo o responsabilidad concreta.
 */
public enum Rol {
    ADMIN,
    REPORTES,
    PRODUCTOS,
    INVENTARIO,
    CLIENTES,
    VENTAS,
    CLIENTE,

    // Se conserva por compatibilidad con bases de datos anteriores del proyecto.
    EMPLEADO
}
