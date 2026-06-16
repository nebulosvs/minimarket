package com.minimarket.entity;

import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UsuarioEntityTest {

    @Test
    void crearUsuario_conDatosObligatorios_contieneInformacionCompleta() {
        Usuario usuario = TestDataFactory.usuarioCompleto("maria", "CLIENTE");

        assertNotNull(usuario);
        assertEquals("Juan", usuario.getNombre());
        assertEquals("Perez", usuario.getApellido());
        assertEquals("maria@minimarket.cl", usuario.getEmail());
        assertEquals("Av. Providencia 100, Santiago", usuario.getDireccion());
    }

    @Test
    void relacionUsuarioVenta_vinculaVentaAlUsuario() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setTotal(5000.0);

        assertSame(usuario, venta.getUsuario());
        assertEquals("cliente", venta.getUsuario().getUsername());
    }

    @Test
    void relacionUsuarioRoles_asignaRolValido() {
        Usuario usuario = TestDataFactory.usuarioCompleto("empleado", "EMPLEADO");

        assertEquals(1, usuario.getRoles().size());
        assertEquals("EMPLEADO", usuario.getRoles().iterator().next().getNombre());
    }

    @Test
    void validarCamposUsuario_simulaPersistencia_conTodosLosDatos() {
        Usuario usuario = new Usuario();
        usuario.setUsername("ana.torres");
        usuario.setNombre("Ana");
        usuario.setApellido("Torres");
        usuario.setEmail("ana.torres@minimarket.cl");
        usuario.setDireccion("Los Leones 456, Providencia");
        usuario.setPassword("hash");

        assertNotNull(usuario.getNombre());
        assertNotNull(usuario.getApellido());
        assertNotNull(usuario.getEmail());
        assertNotNull(usuario.getDireccion());
    }
}
