package com.braintraining.model;

import jakarta.persistence.*;

@Entity
@Table(name = "juegos_usuario")
public class JuegoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "juego_id", nullable = false)
    private JuegoCatalogo juego;

    @Column(nullable = false)
    private boolean activo = true;

    public JuegoUsuario() {}
    public JuegoUsuario(Usuario u, JuegoCatalogo j) {
        this.usuario = u;
        this.juego = j;
        this.activo = true;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario u) { this.usuario = u; }
    public JuegoCatalogo getJuego() { return juego; }
    public void setJuego(JuegoCatalogo j) { this.juego = j; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean a) { this.activo = a; }
}
