package com.braintraining.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "examen_perfil")
public class ExamenPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String objetivo;

    @Column(name = "nivel_experiencia", nullable = false)
    private String nivelExperiencia;

    // infantil | juvenil | adulto
    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private boolean completado = true;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public ExamenPerfil() {}
    public ExamenPerfil(Usuario u, String objetivo, String nivelExp, String categoria) {
        this.usuario = u;
        this.objetivo = objetivo;
        this.nivelExperiencia = nivelExp;
        this.categoria = categoria;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario u) { this.usuario = u; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String o) { this.objetivo = o; }
    public String getNivelExperiencia() { return nivelExperiencia; }
    public void setNivelExperiencia(String n) { this.nivelExperiencia = n; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String c) { this.categoria = c; }
    public boolean isCompletado() { return completado; }
    public LocalDateTime getFecha() { return fecha; }
}
