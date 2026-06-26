package com.minimarket.entity;

import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioEntityTest {

    @Test
    void informacionMovimiento_camposValidos_noSonNulosNiVacios() {
        Producto producto = TestDataFactory.producto(1L, "Aceite", 3500.0, 15);

        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        inventario.validarInformacionMovimiento();

        assertNotNull(inventario.getCantidad());
        assertNotNull(inventario.getTipoMovimiento());
        assertFalse(inventario.getTipoMovimiento().isBlank());
        assertEquals(10, inventario.getCantidad());
        assertEquals("Entrada", inventario.getTipoMovimiento());
    }

    @Test
    void informacionMovimiento_cantidadNula_lanzaExcepcion() {
        Inventario inventario = new Inventario();
        inventario.setTipoMovimiento("Salida");

        assertThrows(IllegalArgumentException.class, inventario::validarInformacionMovimiento);
    }

    @Test
    void informacionMovimiento_tipoMovimientoVacio_lanzaExcepcion() {
        Inventario inventario = new Inventario();
        inventario.setCantidad(5);
        inventario.setTipoMovimiento("   ");

        assertThrows(IllegalArgumentException.class, inventario::validarInformacionMovimiento);
    }

    @Test
    void informacionMovimiento_tipoMovimientoNulo_lanzaExcepcion() {
        Inventario inventario = new Inventario();
        inventario.setCantidad(5);

        assertThrows(IllegalArgumentException.class, inventario::validarInformacionMovimiento);
    }

    @Test
    void relacionInventarioProducto_asociaProductoCorrecto() {
        Producto producto = TestDataFactory.producto(5L, "Azucar", 1500.0, 30);

        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(12);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        assertSame(producto, inventario.getProducto());
        assertEquals(5L, inventario.getProducto().getId());
        assertEquals("Azucar", inventario.getProducto().getNombre());
    }

    @Test
    void registrarMovimiento_entrada_registraCorrectamente() {
        Inventario inventario = new Inventario();

        inventario.registrarMovimiento("Entrada", 15);

        assertEquals("Entrada", inventario.getTipoMovimiento());
        assertEquals(15, inventario.getCantidad());
    }

    @Test
    void registrarMovimiento_salida_registraCorrectamente() {
        Inventario inventario = new Inventario();

        inventario.registrarMovimiento("Salida", 8);

        assertEquals("Salida", inventario.getTipoMovimiento());
        assertEquals(8, inventario.getCantidad());
    }

    @Test
    void registrarMovimiento_tipoInvalido_lanzaExcepcion() {
        Inventario inventario = new Inventario();

        assertThrows(IllegalArgumentException.class,
                () -> inventario.registrarMovimiento("Traslado", 5));
    }

    @Test
    void usuarioTienePermiso_empleado_retornaTrue() {
        Usuario empleado = TestDataFactory.usuarioCompleto("empleado", "EMPLEADO");

        assertTrue(Inventario.usuarioTienePermiso(empleado));
    }

    @Test
    void usuarioTienePermiso_gerente_retornaTrue() {
        Usuario gerente = TestDataFactory.usuarioCompleto("gerente", "GERENTE");

        assertTrue(Inventario.usuarioTienePermiso(gerente));
    }

    @Test
    void usuarioTienePermiso_cliente_retornaFalse() {
        Usuario cliente = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");

        assertFalse(Inventario.usuarioTienePermiso(cliente));
    }

    @Test
    void usuarioTienePermiso_usuarioNulo_retornaFalse() {
        assertFalse(Inventario.usuarioTienePermiso(null));
    }
}
