package com.braintraining.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String juego;

    @Column(nullable = false, length = 50)
    private String dificultad;

    @Column(nullable = false)
    private int puntaje;

    @Column(nullable = false)
    private int nivel = 1;

    @Column(name = "tiempo_seg")
    private Integer tiempoSeg;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // Constructors
    public Record() {}

    public Record(Usuario usuario, String juego, String dificultad, int puntaje, Integer tiempoSeg) {
        this.usuario = usuario;
        this.juego = juego;
        this.dificultad = dificultad;
        this.puntaje = puntaje;
        this.tiempoSeg = tiempoSeg;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getJuego() { return juego; }
    public void setJuego(String juego) { this.juego = juego; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public Integer getTiempoSeg() { return tiempoSeg; }
    public void setTiempoSeg(Integer tiempoSeg) { this.tiempoSeg = tiempoSeg; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
