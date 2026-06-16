package com.minimarket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecursoController {

    @GetMapping("/api/public")
    public String publicResource() {
        return "Bienvenido al recurso publico de MiniMarket Plus";
    }

    @GetMapping("/api/private")
    public String privateResource() {
        return "Acceso autorizado al recurso protegido de MiniMarket Plus.";
    }
}
