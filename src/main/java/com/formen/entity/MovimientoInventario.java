package com.formen.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer stockAntes;

    @Column(nullable = false)
    private Integer stockDespues;

    private String motivo;
    private String usuario;
    private LocalDateTime fecha = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getStockAntes() { return stockAntes; }
    public void setStockAntes(Integer stockAntes) { this.stockAntes = stockAntes; }
    public Integer getStockDespues() { return stockDespues; }
    public void setStockDespues(Integer stockDespues) { this.stockDespues = stockDespues; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
