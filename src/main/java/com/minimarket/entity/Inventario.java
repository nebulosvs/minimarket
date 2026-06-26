package com.minimarket.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
public class Inventario {

    public static final Set<String> TIPOS_MOVIMIENTO_VALIDOS = Set.of("Entrada", "Salida");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private String tipoMovimiento;

    @Column(nullable = false)
    private Date fechaMovimiento;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public void validarInformacionMovimiento() {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad es obligatoria y debe ser mayor a cero");
        }
        if (tipoMovimiento == null || tipoMovimiento.isBlank()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }
        if (!TIPOS_MOVIMIENTO_VALIDOS.contains(tipoMovimiento)) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipoMovimiento);
        }
    }

    public void registrarMovimiento(String tipo, int cantidadMovimiento) {
        this.tipoMovimiento = tipo;
        this.cantidad = cantidadMovimiento;
        validarInformacionMovimiento();
    }

    public static boolean usuarioTienePermiso(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return false;
        }
        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .anyMatch(rol -> "EMPLEADO".equals(rol) || "GERENTE".equals(rol));
    }
}
