package com.minimarket.service.domain;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UsuarioValidationService {

    private static final Set<String> ROLES_VENTA = Set.of("CLIENTE", "EMPLEADO");

    public boolean tieneDatosCompletos(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return isNotBlank(usuario.getNombre())
                && isNotBlank(usuario.getApellido())
                && isNotBlank(usuario.getEmail())
                && isNotBlank(usuario.getDireccion());
    }

    public void validarDatosCompletos(Usuario usuario) {
        if (!tieneDatosCompletos(usuario)) {
            throw new IllegalArgumentException("El usuario no tiene datos obligatorios completos");
        }
    }

    public boolean puedeRegistrarVentas(Usuario usuario) {
        if (!tieneDatosCompletos(usuario) || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return false;
        }
        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .anyMatch(ROLES_VENTA::contains);
    }

    public void validarPermisoRegistroVentas(Usuario usuario) {
        if (!puedeRegistrarVentas(usuario)) {
            throw new IllegalArgumentException("El usuario no tiene permiso para registrar ventas");
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
