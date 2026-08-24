package com.formen.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Min(0)
    @Column(nullable = false)
    private Integer stock = 0;

    @Min(0)
    @Column(nullable = false)
    private Integer stockMinimo = 0;

    private String imagen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private boolean activo = true;

    private LocalDateTime creadoEn = LocalDateTime.now();
    private LocalDateTime actualizadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    /**
     * Devuelve una ruta lista para usar en las vistas.
     * - Si la imagen viene del proyecto original, se sirve desde /imagenes/.
     * - Si la imagen fue subida desde el panel admin, se sirve desde /uploads/.
     */
    public String getImagenUrl() {
        if (imagen == null || imagen.isBlank()) {
            return "/imagenes/polo_casual.jpg";
        }
        String limpia = imagen.trim().replace("\\", "/");
        if (limpia.startsWith("http://") || limpia.startsWith("https://")) {
            return limpia;
        }
        if (limpia.startsWith("/uploads/")) {
            return limpia;
        }
        if (limpia.startsWith("uploads/")) {
            return "/" + limpia;
        }
        if (limpia.startsWith("/imagenes/")) {
            return limpia;
        }
        return "/imagenes/" + limpia;
    }

    public boolean isStockBajo() { return stock != null && stockMinimo != null && stock <= stockMinimo; }
}
