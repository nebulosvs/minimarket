package com.minimarket.service.impl;

import com.minimarket.entity.*;
import com.minimarket.repository.*;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrudServicesImplTest {

    @Nested
    class VentaServiceImplTest {
        @Mock
        private VentaRepository ventaRepository;
        @InjectMocks
        private VentaServiceImpl ventaService;

        @Test
        void findAll_retornaLista() {
            when(ventaRepository.findAll()).thenReturn(List.of(new Venta()));
            assertEquals(1, ventaService.findAll().size());
        }

        @Test
        void findById_existente() {
            Venta venta = new Venta();
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
            assertNotNull(ventaService.findById(1L));
        }

        @Test
        void findById_inexistente() {
            when(ventaRepository.findById(99L)).thenReturn(Optional.empty());
            assertNull(ventaService.findById(99L));
        }

        @Test
        void save_persisteVenta() {
            Venta venta = new Venta();
            when(ventaRepository.save(venta)).thenReturn(venta);
            assertSame(venta, ventaService.save(venta));
        }

        @Test
        void findByUsuarioId_retornaVentas() {
            when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(new Venta()));
            assertEquals(1, ventaService.findByUsuarioId(1L).size());
        }
    }

    @Nested
    class CarritoServiceImplTest {
        @Mock
        private CarritoRepository carritoRepository;
        @InjectMocks
        private CarritoServiceImpl carritoService;

        @Test
        void operacionesCrud() {
            Carrito carrito = new Carrito();
            when(carritoRepository.findAll()).thenReturn(List.of(carrito));
            when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
            when(carritoRepository.save(carrito)).thenReturn(carrito);
            when(carritoRepository.findByUsuarioId(2L)).thenReturn(List.of(carrito));

            assertEquals(1, carritoService.findAll().size());
            assertNotNull(carritoService.findById(1L));
            assertSame(carrito, carritoService.save(carrito));
            carritoService.deleteById(1L);
            assertEquals(1, carritoService.findByUsuarioId(2L).size());

            verify(carritoRepository).deleteById(1L);
        }

        @Test
        void findById_inexistente() {
            when(carritoRepository.findById(99L)).thenReturn(Optional.empty());
            assertNull(carritoService.findById(99L));
        }
    }

    @Nested
    class InventarioServiceImplTest {
        @Mock
        private InventarioRepository inventarioRepository;
        @InjectMocks
        private InventarioServiceImpl inventarioService;

        @Test
        void operacionesCrud() {
            Inventario inventario = new Inventario();
            when(inventarioRepository.findAll()).thenReturn(List.of(inventario));
            when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
            when(inventarioRepository.save(inventario)).thenReturn(inventario);
            when(inventarioRepository.findByProductoId(3L)).thenReturn(List.of(inventario));

            assertEquals(1, inventarioService.findAll().size());
            assertNotNull(inventarioService.findById(1L));
            assertSame(inventario, inventarioService.save(inventario));
            inventarioService.deleteById(1L);
            assertEquals(1, inventarioService.findByProductoId(3L).size());
        }
    }

    @Nested
    class DetalleVentaServiceImplTest {
        @Mock
        private DetalleVentaRepository detalleVentaRepository;
        @InjectMocks
        private DetalleVentaServiceImpl detalleVentaService;

        @Test
        void operacionesCrud() {
            DetalleVenta detalle = new DetalleVenta();
            when(detalleVentaRepository.findAll()).thenReturn(List.of(detalle));
            when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalle));
            when(detalleVentaRepository.save(detalle)).thenReturn(detalle);
            when(detalleVentaRepository.findByVentaId(5L)).thenReturn(List.of(detalle));

            assertEquals(1, detalleVentaService.findAll().size());
            assertNotNull(detalleVentaService.findById(1L));
            assertSame(detalle, detalleVentaService.save(detalle));
            detalleVentaService.deleteById(1L);
            assertEquals(1, detalleVentaService.findByVentaId(5L).size());
        }
    }

    @Nested
    class ProductoServiceImplTest {
        @Mock
        private ProductoRepository productoRepository;
        @InjectMocks
        private ProductoServiceImpl productoService;

        @Test
        void operacionesCrud() {
            Producto producto = TestDataFactory.producto(1L, "Arroz", 1000, 10);
            when(productoRepository.findAll()).thenReturn(List.of(producto));
            when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productoRepository.save(producto)).thenReturn(producto);
            when(productoRepository.findByCategoriaId(2L)).thenReturn(List.of(producto));

            assertEquals(1, productoService.findAll().size());
            assertNotNull(productoService.findById(1L));
            assertSame(producto, productoService.save(producto));
            productoService.deleteById(1L);
            assertEquals(1, productoService.findByCategoriaId(2L).size());
        }
    }

    @Nested
    class RolServiceImplTest {
        @Mock
        private RolRepository rolRepository;
        @InjectMocks
        private RolServiceImpl rolService;

        @Test
        void findByNombre_retornaRol() {
            Rol rol = new Rol();
            rol.setNombre("CLIENTE");
            when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rol));
            assertEquals("CLIENTE", rolService.findByNombre("CLIENTE").get().getNombre());
        }
    }
}
