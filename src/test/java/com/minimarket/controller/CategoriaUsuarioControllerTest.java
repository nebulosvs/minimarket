package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.UsuarioService;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    @Test
    void listarCategorias_retornaLista() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Lacteos");
        when(categoriaService.findAll()).thenReturn(List.of(categoria));
        assertEquals(1, categoriaController.listarCategorias().size());
    }

    @Test
    void obtenerCategoriaPorId_existente() {
        when(categoriaService.findById(1L)).thenReturn(new Categoria());
        assertEquals(HttpStatus.OK, categoriaController.obtenerCategoriaPorId(1L).getStatusCode());
    }

    @Test
    void obtenerCategoriaPorId_inexistente() {
        when(categoriaService.findById(99L)).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, categoriaController.obtenerCategoriaPorId(99L).getStatusCode());
    }

    @Test
    void guardarCategoria_delegaEnServicio() {
        Categoria categoria = new Categoria();
        when(categoriaService.save(categoria)).thenReturn(categoria);
        assertEquals(categoria, categoriaController.guardarCategoria(categoria));
    }

    @Test
    void actualizarCategoria_existente() {
        Categoria categoria = new Categoria();
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoria);
        assertEquals(HttpStatus.OK, categoriaController.actualizarCategoria(1L, categoria).getStatusCode());
    }

    @Test
    void eliminarCategoria_existente() {
        when(categoriaService.findById(1L)).thenReturn(new Categoria());
        assertEquals(HttpStatus.NO_CONTENT, categoriaController.eliminarCategoria(1L).getStatusCode());
        verify(categoriaService).deleteById(1L);
    }
}

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void listarUsuarios_retornaLista() {
        when(usuarioService.findAll()).thenReturn(List.of(TestDataFactory.usuarioCompleto("cliente", "CLIENTE")));
        assertEquals(1, usuarioController.listarUsuarios().size());
    }

    @Test
    void obtenerUsuarioPorId_existente() {
        when(usuarioService.findById(1L))
                .thenReturn(Optional.of(TestDataFactory.usuarioCompleto("cliente", "CLIENTE")));
        assertEquals(HttpStatus.OK, usuarioController.obtenerUsuarioPorId(1L).getStatusCode());
    }

    @Test
    void obtenerUsuarioPorId_inexistente() {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, usuarioController.obtenerUsuarioPorId(99L).getStatusCode());
    }

    @Test
    void guardarUsuario_delegaEnServicio() {
        Usuario usuario = TestDataFactory.usuarioCompleto("nuevo", "CLIENTE");
        when(usuarioService.save(usuario)).thenReturn(usuario);
        assertEquals(usuario, usuarioController.guardarUsuario(usuario));
    }

    @Test
    void actualizarUsuario_existente() {
        Usuario usuario = TestDataFactory.usuarioCompleto("nuevo", "CLIENTE");
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
        assertEquals(HttpStatus.OK, usuarioController.actualizarUsuario(1L, usuario).getStatusCode());
    }

    @Test
    void eliminarUsuario_existente() {
        when(usuarioService.findById(1L))
                .thenReturn(Optional.of(TestDataFactory.usuarioCompleto("cliente", "CLIENTE")));
        assertEquals(HttpStatus.NO_CONTENT, usuarioController.eliminarUsuario(1L).getStatusCode());
        verify(usuarioService).deleteById(1L);
    }
}
