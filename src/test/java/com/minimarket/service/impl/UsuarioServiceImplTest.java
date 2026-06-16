package com.minimarket.service.impl;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void findAll_delegaEnRepositorio() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.findAll().size());
    }

    @Test
    void findById_retornaOptional() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertTrue(usuarioService.findById(1L).isPresent());
    }

    @Test
    void findByUsername_retornaOptional() {
        Usuario usuario = TestDataFactory.usuarioCompleto("cliente", "CLIENTE");
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));

        assertTrue(usuarioService.findByUsername("cliente").isPresent());
    }

    @Test
    void save_codificaPasswordPlano() {
        Usuario usuario = TestDataFactory.usuarioCompleto("nuevo", "CLIENTE");
        usuario.setPassword("Plano123!");
        when(passwordEncoder.encode("Plano123!")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario guardado = usuarioService.save(usuario);

        assertEquals("$2a$10$hash", guardado.getPassword());
        verify(passwordEncoder).encode("Plano123!");
    }

    @Test
    void save_mantienePasswordBcrypt() {
        Usuario usuario = TestDataFactory.usuarioCompleto("nuevo", "CLIENTE");
        usuario.setPassword("$2a$10$existente");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario guardado = usuarioService.save(usuario);

        assertEquals("$2a$10$existente", guardado.getPassword());
    }

    @Test
    void deleteById_delegaEnRepositorio() {
        usuarioService.deleteById(5L);

        verify(usuarioRepository).deleteById(5L);
    }
}
