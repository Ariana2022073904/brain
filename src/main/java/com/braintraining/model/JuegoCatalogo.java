package com.braintraining.model;

import jakarta.persistence.*;

@Entity
@Table(name = "juegos_catalogo")
public class JuegoCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "categoria_edad", nullable = false)
    private String categoriaEdad;

    @Column(nullable = false)
    private String habilidad;

    @Column(nullable = false)
    private String icono;

    @Column(nullable = false)
    private String url;

    public JuegoCatalogo() {}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoriaEdad() { return categoriaEdad; }
    public String getHabilidad() { return habilidad; }
    public String getIcono() { return icono; }
    public String getUrl() { return url; }
    public void setNombre(String n) { this.nombre = n; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public void setCategoriaEdad(String c) { this.categoriaEdad = c; }
    public void setHabilidad(String h) { this.habilidad = h; }
    public void setIcono(String i) { this.icono = i; }
    public void setUrl(String u) { this.url = u; }
}
