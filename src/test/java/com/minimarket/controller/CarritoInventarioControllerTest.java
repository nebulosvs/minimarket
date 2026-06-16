package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Inventario;
import com.minimarket.service.CarritoService;
import com.minimarket.service.InventarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    @Mock
    private CarritoService carritoService;

    @InjectMocks
    private CarritoController carritoController;

    @Test
    void operacionesCarrito() {
        Carrito carrito = new Carrito();
        when(carritoService.findAll()).thenReturn(List.of(carrito));
        when(carritoService.findById(1L)).thenReturn(carrito);
        when(carritoService.save(any(Carrito.class))).thenReturn(carrito);

        assertEquals(1, carritoController.listarCarrito().size());
        assertEquals(HttpStatus.OK, carritoController.obtenerCarritoPorId(1L).getStatusCode());
        assertEquals(carrito, carritoController.agregarProductoAlCarrito(carrito));
        assertEquals(HttpStatus.OK, carritoController.actualizarCarrito(1L, carrito).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, carritoController.eliminarProductoDelCarrito(1L).getStatusCode());
        verify(carritoService).deleteById(1L);
    }

    @Test
    void obtenerCarrito_inexistente() {
        when(carritoService.findById(99L)).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, carritoController.obtenerCarritoPorId(99L).getStatusCode());
    }
}

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    @Test
    void operacionesInventario() {
        Inventario inventario = new Inventario();
        when(inventarioService.findAll()).thenReturn(List.of(inventario));
        when(inventarioService.findById(1L)).thenReturn(inventario);
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        assertEquals(1, inventarioController.listarMovimientosDeInventario().size());
        assertEquals(HttpStatus.OK, inventarioController.obtenerMovimientoPorId(1L).getStatusCode());
        assertEquals(inventario, inventarioController.registrarMovimiento(inventario));
        assertEquals(HttpStatus.OK, inventarioController.actualizarMovimiento(1L, inventario).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, inventarioController.eliminarMovimiento(1L).getStatusCode());
    }
}
