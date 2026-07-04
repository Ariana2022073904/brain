package com.braintraining.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "misiones_completadas")
public class MisionCompletada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "mision_index", nullable = false)
    private int misionIndex;

    public MisionCompletada() {}

    public MisionCompletada(Usuario usuario, LocalDate fecha, int misionIndex) {
        this.usuario     = usuario;
        this.fecha       = fecha;
        this.misionIndex = misionIndex;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario u) { this.usuario = u; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate f) { this.fecha = f; }
    public int getMisionIndex() { return misionIndex; }
    public void setMisionIndex(int i) { this.misionIndex = i; }
}
