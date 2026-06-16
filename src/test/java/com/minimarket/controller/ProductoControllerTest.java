package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    @Test
    void listarProductos_retornaLista() {
        when(productoService.findAll()).thenReturn(List.of(TestDataFactory.producto(1L, "Arroz", 1000, 5)));

        assertEquals(1, productoController.listarProductos().size());
    }

    @Test
    void obtenerProductoPorId_existente() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 1000, 5);
        when(productoService.findById(1L)).thenReturn(producto);

        ResponseEntity<Producto> response = productoController.obtenerProductoPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void obtenerProductoPorId_inexistente() {
        when(productoService.findById(99L)).thenReturn(null);

        ResponseEntity<Producto> response = productoController.obtenerProductoPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void guardarProducto_delegaEnServicio() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 1000, 5);
        when(productoService.save(producto)).thenReturn(producto);

        assertSameProducto(producto, productoController.guardarProducto(producto));
    }

    @Test
    void actualizarProducto_existente() {
        Producto producto = TestDataFactory.producto(1L, "Arroz", 1000, 5);
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        ResponseEntity<Producto> response = productoController.actualizarProducto(1L, producto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void actualizarProducto_inexistente() {
        when(productoService.findById(99L)).thenReturn(null);

        ResponseEntity<Producto> response = productoController.actualizarProducto(99L, new Producto());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void eliminarProducto_existente() {
        when(productoService.findById(1L)).thenReturn(TestDataFactory.producto(1L, "Arroz", 1000, 5));

        ResponseEntity<Void> response = productoController.eliminarProducto(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productoService).deleteById(1L);
    }

    @Test
    void eliminarProducto_inexistente() {
        when(productoService.findById(99L)).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND, productoController.eliminarProducto(99L).getStatusCode());
    }

    private void assertSameProducto(Producto expected, Producto actual) {
        assertEquals(expected.getNombre(), actual.getNombre());
    }
}
