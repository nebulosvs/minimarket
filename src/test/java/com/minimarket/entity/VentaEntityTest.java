package com.minimarket.entity;

import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    private DetalleVenta detalle(Producto producto, int cantidad, double precio) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecio(precio);
        return detalle;
    }
}
