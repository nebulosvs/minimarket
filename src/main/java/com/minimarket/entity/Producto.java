package com.minimarket.entity;

import jakarta.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Getters y Setters
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void actualizarDatos(String nombre, Double precio, Integer stock) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (precio == null || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.nombre = nombre.trim();
        this.precio = precio;
        this.stock = stock;
    }

    public static boolean puedeSerModificadoPor(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return false;
        }
        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .anyMatch("GERENTE"::equals);
    }
}
