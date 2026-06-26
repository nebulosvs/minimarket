package com.minimarket.service.domain;

import com.minimarket.entity.Usuario;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UsuarioValidationServiceTest {

    @InjectMocks
    private UsuarioValidationService usuarioValidationService;

    private Usuario usuarioCliente;

    @BeforeEach
    void setUp() {
        usuarioCliente = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
    }

    @Test
    void tieneDatosCompletos_conTodosLosCampos_retornaTrue() {
        assertTrue(usuarioValidationService.tieneDatosCompletos(usuarioCliente));
    }

    @Test
    void tieneDatosCompletos_sinEmail_retornaFalse() {
        usuarioCliente.setEmail(" ");

        assertFalse(usuarioValidationService.tieneDatosCompletos(usuarioCliente));
    }

    @Test
    void tieneDatosCompletos_usuarioNulo_retornaFalse() {
        assertFalse(usuarioValidationService.tieneDatosCompletos(null));
    }

    @Test
    void tieneDatosCompletos_sinDireccion_retornaFalse() {
        usuarioCliente.setDireccion(null);

        assertFalse(usuarioValidationService.tieneDatosCompletos(usuarioCliente));
    }

    @Test
    void puedeRegistrarVentas_conRolCliente_retornaFalse() {
        assertFalse(usuarioValidationService.puedeRegistrarVentas(usuarioCliente));
    }

    @Test
    void puedeRegistrarVentas_conRolEmpleado_retornaTrue() {
        Usuario empleado = TestDataFactory.usuarioCompleto("empleado", "EMPLEADO");

        assertTrue(usuarioValidationService.puedeRegistrarVentas(empleado));
    }

    @Test
    void puedeRegistrarVentas_conRolGerente_retornaFalse() {
        Usuario gerente = TestDataFactory.usuarioCompleto("gerente", "GERENTE");

        assertFalse(usuarioValidationService.puedeRegistrarVentas(gerente));
    }

    @Test
    void validarDatosCompletos_datosIncompletos_lanzaExcepcion() {
        Usuario incompleto = TestDataFactory.usuarioIncompleto();

        assertThrows(IllegalArgumentException.class,
                () -> usuarioValidationService.validarDatosCompletos(incompleto));
    }

    @Test
    void validarPermisoRegistroVentas_usuarioSinRolValido_lanzaExcepcion() {
        Usuario gerente = TestDataFactory.usuarioCompleto("gerente", "GERENTE");

        assertThrows(IllegalArgumentException.class,
                () -> usuarioValidationService.validarPermisoRegistroVentas(gerente));
    }
}
