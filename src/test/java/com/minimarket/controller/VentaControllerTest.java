package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import com.minimarket.service.domain.VentaBusinessService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private VentaBusinessService ventaBusinessService;

    @InjectMocks
    private VentaController ventaController;

    @Test
    void listarVentas_retornaLista() {
        when(ventaService.findAll()).thenReturn(List.of(new Venta()));
        assertEquals(1, ventaController.listarVentas().size());
    }

    @Test
    void obtenerVentaPorId_existente() {
        when(ventaService.findById(1L)).thenReturn(new Venta());
        ResponseEntity<Venta> response = ventaController.obtenerVentaPorId(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void obtenerVentaPorId_inexistente() {
        when(ventaService.findById(99L)).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, ventaController.obtenerVentaPorId(99L).getStatusCode());
    }

    @Test
    void guardarVenta_delegaEnBusinessService() {
        Venta venta = new Venta();
        when(ventaBusinessService.registrarVenta(venta)).thenReturn(venta);
        assertEquals(venta, ventaController.guardarVenta(venta));
    }
}
