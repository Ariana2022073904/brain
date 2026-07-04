package com.braintraining.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "misiones")
public class Mision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "generada_en")
    private LocalDateTime generadaEn = LocalDateTime.now();

    public Mision() {}

    public Mision(Usuario usuario, LocalDate fecha, String contenido) {
        this.usuario  = usuario;
        this.fecha    = fecha;
        this.contenido = contenido;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario u) { this.usuario = u; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate f) { this.fecha = f; }
    public String getContenido() { return contenido; }
    public void setContenido(String c) { this.contenido = c; }
    public LocalDateTime getGeneradaEn() { return generadaEn; }
    public void setGeneradaEn(LocalDateTime g) { this.generadaEn = g; }
}
