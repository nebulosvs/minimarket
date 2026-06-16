package com.minimarket.service.domain;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaBusinessServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioValidationService usuarioValidationService;

    @InjectMocks
    private VentaBusinessService ventaBusinessService;

    private Producto leche;
    private Producto pan;

    @BeforeEach
    void setUp() {
        leche = TestDataFactory.producto(1L, "Leche", 1990.0, 10);
        pan = TestDataFactory.producto(2L, "Pan", 990.0, 5);
    }

    @Test
    void calcularTotal_variosProductos_sumaCorrectamente() {
        List<DetalleVenta> detalles = List.of(
                detalle(leche, 2, 1990.0),
                detalle(pan, 3, 990.0)
        );

        double total = ventaBusinessService.calcularTotal(detalles);

        assertEquals(6950.0, total);
    }

    @Test
    void calcularTotal_sinDetalles_retornaCero() {
        assertEquals(0.0, ventaBusinessService.calcularTotal(new ArrayList<>()));
        assertEquals(0.0, ventaBusinessService.calcularTotal(null));
    }

    @Test
    void validarStockDisponible_stockSuficiente_noLanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(leche));

        ventaBusinessService.validarStockDisponible(List.of(detalle(leche, 2, 1990.0)));
    }

    @Test
    void validarStockDisponible_stockInsuficiente_lanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(leche));

        assertThrows(IllegalArgumentException.class,
                () -> ventaBusinessService.validarStockDisponible(List.of(detalle(leche, 20, 1990.0))));
    }

    @Test
    void validarStockDisponible_productoNoEncontrado_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Producto inexistente = TestDataFactory.producto(99L, "Inexistente", 100.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> ventaBusinessService.validarStockDisponible(List.of(detalle(inexistente, 1, 100.0))));
    }

    @Test
    void registrarVenta_flujoValido_guardaVentaConTotal() {
        Venta venta = ventaValida(List.of(detalle(leche, 2, 1990.0)));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(venta.getUsuario()));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(leche));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Venta resultado = ventaBusinessService.registrarVenta(venta);

        assertEquals(3980.0, resultado.getTotal());
        verify(usuarioValidationService).validarDatosCompletos(venta.getUsuario());
        verify(usuarioValidationService).validarPermisoRegistroVentas(venta.getUsuario());
        verify(productoRepository).save(any(Producto.class));
        verify(ventaRepository).save(venta);
    }

    @Test
    void registrarVenta_sinUsuario_lanzaExcepcion() {
        Venta venta = ventaValida(List.of(detalle(leche, 1, 1990.0)));
        venta.setUsuario(new com.minimarket.entity.Usuario());

        assertThrows(IllegalArgumentException.class, () -> ventaBusinessService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void registrarVenta_usuarioIncompleto_lanzaExcepcion() {
        Venta venta = ventaValida(List.of(detalle(leche, 1, 1990.0)));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(venta.getUsuario()));
        org.mockito.Mockito.doThrow(new IllegalArgumentException("El usuario no tiene datos obligatorios completos"))
                .when(usuarioValidationService)
                .validarDatosCompletos(venta.getUsuario());

        assertThrows(IllegalArgumentException.class, () -> ventaBusinessService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any());
    }

    private Venta ventaValida(List<DetalleVenta> detalles) {
        Venta venta = new Venta();
        venta.setUsuario(TestDataFactory.usuarioCompleto("cliente", "CLIENTE"));
        venta.setFecha(new Date());
        venta.setDetalles(detalles);
        return venta;
    }

    private DetalleVenta detalle(Producto producto, int cantidad, double precio) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecio(precio);
        return detalle;
    }
}
