package com.minimarket.service.domain;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaBusinessService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioValidationService usuarioValidationService;

    public VentaBusinessService(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioValidationService usuarioValidationService
    ) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioValidationService = usuarioValidationService;
    }

    public double calcularTotal(List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        return detalles.stream()
                .mapToDouble(detalle -> detalle.getPrecio() * detalle.getCantidad())
                .sum();
    }

    public void validarStockDisponible(List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto");
        }

        for (DetalleVenta detalle : detalles) {
            Producto productoReferencia = detalle.getProducto();
            if (productoReferencia == null || productoReferencia.getId() == null) {
                throw new IllegalArgumentException("Producto inválido en detalle de venta");
            }

            Producto producto = productoRepository.findById(productoReferencia.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto: " + producto.getNombre());
            }
        }
    }

    public Venta registrarVenta(Venta venta) {
        if (venta.getUsuario() == null || venta.getUsuario().getId() == null) {
            throw new IllegalArgumentException("La venta debe estar vinculada a un usuario válido");
        }

        Usuario usuario = usuarioRepository.findById(venta.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        venta.setUsuario(usuario);

        usuarioValidationService.validarDatosCompletos(venta.getUsuario());
        usuarioValidationService.validarPermisoRegistroVentas(venta.getUsuario());
        validarStockDisponible(venta.getDetalles());

        venta.setTotal(calcularTotal(venta.getDetalles()));
        actualizarStock(venta.getDetalles());

        for (DetalleVenta detalle : venta.getDetalles()) {
            detalle.setVenta(venta);
        }

        return ventaRepository.save(venta);
    }

    private void actualizarStock(List<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getProducto().getId()).orElseThrow();
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }
    }
}
