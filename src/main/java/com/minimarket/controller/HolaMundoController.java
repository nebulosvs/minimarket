package com.minimarket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HolaMundoController {

    @GetMapping("/public/hola")
    public String holaMundo() {
        return "¡Hola Mundo!";
    }

    @GetMapping("/app/index_normal")
    public String endpointPublicoSemana1() {
        return "¡Bienvenido al sitio público de Empresa X!";
    }

    @GetMapping("/app/index_protegido")
    public String endpointProtegidoSemana1() {
        return "¡Bienvenido al área protegida de Empresa X! Necesitas estar autenticado para ver este contenido.";
    }
}
