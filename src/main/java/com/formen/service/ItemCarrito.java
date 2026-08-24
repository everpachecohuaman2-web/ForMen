package com.formen.service;

import com.formen.entity.Producto;
import java.math.BigDecimal;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    public ItemCarrito() {}
    public ItemCarrito(Producto producto, int cantidad) { this.producto = producto; this.cantidad = cantidad; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getSubtotal() { return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)); }
}
