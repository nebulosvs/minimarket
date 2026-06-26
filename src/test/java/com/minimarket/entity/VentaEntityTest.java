package com.minimarket.entity;

import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentaEntityTest {

    @Test
    void relacionVentaDetalleProducto_mantieneAsociaciones() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        Producto producto = TestDataFactory.producto(1L, "Arroz", 2500.0, 15);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecio(2500.0);

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(List.of(detalle));
        detalle.setVenta(venta);

        assertSame(usuario, venta.getUsuario());
        assertSame(producto, venta.getDetalles().get(0).getProducto());
        assertSame(venta, venta.getDetalles().get(0).getVenta());
    }

    @Test
    void calcularTotalManual_sumaPrecioPorCantidad() {
        DetalleVenta detalleUno = detalle(TestDataFactory.producto(1L, "Aceite", 3000.0, 4), 1, 3000.0);
        DetalleVenta detalleDos = detalle(TestDataFactory.producto(2L, "Azucar", 1500.0, 8), 2, 1500.0);

        double total = detalleUno.getPrecio() * detalleUno.getCantidad()
                + detalleDos.getPrecio() * detalleDos.getCantidad();

        assertEquals(6000.0, total);
    }

    @Test
    void relacionVentaUsuario_validaIdentificadorDeUsuario() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        usuario.setId(7L);

        Venta venta = new Venta();
        venta.setUsuario(usuario);

        assertEquals(7L, venta.getUsuario().getId());
        assertEquals("cliente@minimarket.cl", venta.getUsuario().getEmail());
    }

    @Test
    void calcularTotalDesdeDetalles_sumaCorrectamente() {
        Venta venta = new Venta();
        venta.setDetalles(List.of(
                detalle(TestDataFactory.producto(1L, "Aceite", 3000.0, 4), 2, 3000.0),
                detalle(TestDataFactory.producto(2L, "Azucar", 1500.0, 8), 1, 1500.0)
        ));

        assertEquals(7500.0, venta.calcularTotalDesdeDetalles());
    }

    @Test
    void detallesReflejanProductosVendidos_conDetallesValidos_retornaTrue() {
        Venta venta = new Venta();
        venta.setDetalles(List.of(
                detalle(TestDataFactory.producto(1L, "Leche", 1990.0, 10), 3, 1990.0)
        ));

        assertTrue(venta.detallesReflejanProductosVendidos());
    }

    @Test
    void detallesReflejanProductosVendidos_sinProducto_retornaFalse() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(2);
        detalle.setPrecio(1000.0);

        Venta venta = new Venta();
        venta.setDetalles(List.of(detalle));

        assertFalse(venta.detallesReflejanProductosVendidos());
    }

    @Test
    void detallesReflejanProductosVendidos_listaVacia_retornaFalse() {
        Venta venta = new Venta();
        venta.setDetalles(List.of());

        assertFalse(venta.detallesReflejanProductosVendidos());
    }

    @Test
    void puedeSerRegistradaPor_empleado_retornaTrue() {
        Usuario empleado = TestDataFactory.usuarioCompleto("empleado", "EMPLEADO");

        assertTrue(Venta.puedeSerRegistradaPor(empleado));
    }

    @Test
    void puedeSerRegistradaPor_cliente_retornaFalse() {
        Usuario cliente = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");

        assertFalse(Venta.puedeSerRegistradaPor(cliente));
    }

    @Test
    void puedeSerRegistradaPor_gerente_retornaFalse() {
        Usuario gerente = TestDataFactory.usuarioCompleto("gerente", "GERENTE");

        assertFalse(Venta.puedeSerRegistradaPor(gerente));
    }

    @Test
    void ventaConDetalles_reflejaProductosYCantidadesVendidas() {
        Producto arroz = TestDataFactory.producto(1L, "Arroz", 2500.0, 20);
        Producto aceite = TestDataFactory.producto(2L, "Aceite", 3500.0, 15);
        Usuario cajero = TestDataFactory.usuarioCompleto("empleado", "EMPLEADO");

        DetalleVenta detalleArroz = detalle(arroz, 2, 2500.0);
        DetalleVenta detalleAceite = detalle(aceite, 1, 3500.0);

        Venta venta = new Venta();
        venta.setUsuario(cajero);
        venta.setFecha(new Date());
        venta.setDetalles(List.of(detalleArroz, detalleAceite));

        assertTrue(Venta.puedeSerRegistradaPor(cajero));
        assertTrue(venta.detallesReflejanProductosVendidos());
        assertEquals(8500.0, venta.calcularTotalDesdeDetalles());
        assertEquals("Arroz", venta.getDetalles().get(0).getProducto().getNombre());
        assertEquals(2, venta.getDetalles().get(0).getCantidad());
        assertEquals("Aceite", venta.getDetalles().get(1).getProducto().getNombre());
    }

    private DetalleVenta detalle(Producto producto, int cantidad, double precio) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecio(precio);
        return detalle;
    }
}
