package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
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
class DetalleVentaControllerTest {

    @Mock
    private DetalleVentaService detalleVentaService;

    @InjectMocks
    private DetalleVentaController detalleVentaController;

    @Test
    void operacionesDetalleVenta() {
        DetalleVenta detalle = new DetalleVenta();
        when(detalleVentaService.findAll()).thenReturn(List.of(detalle));
        when(detalleVentaService.findById(1L)).thenReturn(detalle);
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalle);

        assertEquals(1, detalleVentaController.listarDetalleVentas().size());
        assertEquals(HttpStatus.OK, detalleVentaController.obtenerDetalleVentaPorId(1L).getStatusCode());
        assertEquals(detalle, detalleVentaController.guardarDetalleVenta(detalle));
        assertEquals(HttpStatus.OK, detalleVentaController.actualizarDetalleVenta(1L, detalle).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, detalleVentaController.eliminarDetalleVenta(1L).getStatusCode());
        verify(detalleVentaService).deleteById(1L);
    }
}
