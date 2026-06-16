package com.minimarket.support;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;

import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Usuario usuarioCompleto(String username, String rolNombre) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail(username + "@minimarket.cl");
        usuario.setDireccion("Av. Providencia 100, Santiago");
        usuario.setPassword("hash");

        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre(rolNombre);
        usuario.setRoles(Set.of(rol));
        return usuario;
    }

    public static Usuario usuarioIncompleto() {
        Usuario usuario = new Usuario();
        usuario.setUsername("incompleto");
        usuario.setPassword("hash");
        return usuario;
    }

    public static Producto producto(Long id, String nombre, double precio, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        return producto;
    }
}
