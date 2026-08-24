package com.formen.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "correo"))
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 80, message = "Los nombres deben tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "Los nombres no deben contener números ni símbolos")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 80, message = "Los apellidos deben tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "Los apellidos no deben contener números ni símbolos")
    private String apellidos;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^9\\d{8}$", message = "El teléfono debe tener 9 dígitos y empezar con 9")
    private String telefono;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.CLIENTE;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private String origenRegistro = "WEB";

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getOrigenRegistro() { return origenRegistro; }
    public void setOrigenRegistro(String origenRegistro) { this.origenRegistro = origenRegistro; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getNombreCompleto() { return (nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos); }
}
