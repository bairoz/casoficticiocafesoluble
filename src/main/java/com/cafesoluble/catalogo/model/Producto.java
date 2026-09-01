package com.cafesoluble.catalogo.model;

public class Producto {

    private Long id;
    private String nombre;
    private String presentacion;
    private String categoria;
    private boolean disponible;

    public Producto() {
    }

    public Producto(Long id, String nombre, String presentacion, String categoria, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.presentacion = presentacion;
        this.categoria = categoria;
        this.disponible = disponible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
