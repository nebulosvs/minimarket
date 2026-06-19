package com.minimarket.entity;

import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarritoEntityTest {

    @Test
    void agregarProducto_stockSuficiente_agregaProductoAlCarrito() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 10);

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carrito.agregarProducto(producto, 5);

        assertSame(producto, carrito.getProducto());
        assertEquals(5, carrito.getCantidad());
    }

    @Test
    void agregarProducto_stockInsuficiente_lanzaExcepcion() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 3);

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);

        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto(producto, 5));
    }

    @Test
    void agregarProducto_cantidadIgualAlStock_permiteAgregar() {
        Producto producto = TestDataFactory.producto(2L, "Leche", 1990.0, 8);

        Carrito carrito = new Carrito();
        carrito.agregarProducto(producto, 8);

        assertEquals(8, carrito.getCantidad());
        assertEquals(8, carrito.getProducto().getStock());
    }

    @Test
    void relacionCarritoUsuario_asociaUsuarioCorrecto() {
        Usuario usuario = TestDataFactory.usuarioCompleto("maria", "CLIENTE");
        usuario.setId(12L);
        Producto producto = TestDataFactory.producto(1L, "Pan", 990.0, 20);

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carrito.agregarProducto(producto, 2);

        assertSame(usuario, carrito.getUsuario());
        assertEquals(12L, carrito.getUsuario().getId());
        assertEquals("maria@minimarket.cl", carrito.getUsuario().getEmail());
    }
}
