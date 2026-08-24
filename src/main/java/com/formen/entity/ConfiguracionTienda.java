package com.formen.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_tienda")
public class ConfiguracionTienda {
    @Id
    private Long id = 1L;
    private String nombreTienda = "ForMen";
    private String whatsapp = "951478963";
    private String correo = "formen@gmail.pe";
    private String direccion = "Feria Balta 258 - Chiclayo";
    private String numeroYape = "951478963";
    private String numeroPlin = "951478963";
    private String mensajeBienvenida = "Moda masculina exclusiva";
    private boolean ocultarSinStock = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getNumeroYape() { return numeroYape; }
    public void setNumeroYape(String numeroYape) { this.numeroYape = numeroYape; }
    public String getNumeroPlin() { return numeroPlin; }
    public void setNumeroPlin(String numeroPlin) { this.numeroPlin = numeroPlin; }
    public String getMensajeBienvenida() { return mensajeBienvenida; }
    public void setMensajeBienvenida(String mensajeBienvenida) { this.mensajeBienvenida = mensajeBienvenida; }
    public boolean isOcultarSinStock() { return ocultarSinStock; }
    public void setOcultarSinStock(boolean ocultarSinStock) { this.ocultarSinStock = ocultarSinStock; }
}
