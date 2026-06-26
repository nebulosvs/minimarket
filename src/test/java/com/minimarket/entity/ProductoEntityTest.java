package com.minimarket.entity;

import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoEntityTest {

    @Test
    void actualizarDatos_conValoresValidos_actualizaProducto() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 10);

        producto.actualizarDatos("Arroz Premium", 2990.0, 25);

        assertEquals("Arroz Premium", producto.getNombre());
        assertEquals(2990.0, producto.getPrecio());
        assertEquals(25, producto.getStock());
    }

    @Test
    void actualizarDatos_nombreVacio_lanzaExcepcion() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> producto.actualizarDatos("  ", 2500.0, 10));
    }

    @Test
    void actualizarDatos_precioInvalido_lanzaExcepcion() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> producto.actualizarDatos("Arroz", 0.0, 10));
    }

    @Test
    void actualizarDatos_stockNegativo_lanzaExcepcion() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 10);

        assertThrows(IllegalArgumentException.class,
                () -> producto.actualizarDatos("Arroz", 2500.0, -1));
    }

    @Test
    void puedeSerModificadoPor_gerente_retornaTrue() {
        Usuario gerente = TestDataFactory.usuarioCompleto("gerente", "GERENTE");

        assertTrue(Producto.puedeSerModificadoPor(gerente));
    }

    @Test
    void puedeSerModificadoPor_empleado_retornaFalse() {
        Usuario empleado = TestDataFactory.usuarioCompleto("empleado", "EMPLEADO");

        assertFalse(Producto.puedeSerModificadoPor(empleado));
    }

    @Test
    void puedeSerModificadoPor_cliente_retornaFalse() {
        Usuario cliente = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");

        assertFalse(Producto.puedeSerModificadoPor(cliente));
    }

    @Test
    void puedeSerModificadoPor_usuarioSinRoles_retornaFalse() {
        Usuario usuario = new Usuario();
        usuario.setUsername("sin.rol");

        assertFalse(Producto.puedeSerModificadoPor(usuario));
    }

    @Test
    void puedeSerModificadoPor_usuarioNulo_retornaFalse() {
        assertFalse(Producto.puedeSerModificadoPor(null));
    }
}
