package com.braintraining.controller;

import com.braintraining.model.*;
import com.braintraining.repository.*;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.List;

@Controller
public class ExamenController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private ExamenPerfilRepository examenRepo;
    @Autowired private JuegoCatalogoRepository catalogoRepo;
    @Autowired private JuegoUsuarioRepository juegoUsuarioRepo;

    @GetMapping("/examen")
    public String examenPage(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        // Si ya completó el examen, redirigir al menú
        if (examenRepo.existsByUsuario(usuario)) {
            return "redirect:/menu";
        }
        model.addAttribute("usuario", usuario);
        return "examen";
    }

    @PostMapping("/examen")
    public String procesarExamen(@RequestParam String objetivo,
                                  @RequestParam String nivelExperiencia,
                                  @RequestParam int edadPerfil,
                                  Authentication auth) {

        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();

        // Determinar categoría según edad
        String categoria = determinarCategoria(edadPerfil);

        // Guardar perfil
        ExamenPerfil perfil = new ExamenPerfil(usuario, objetivo, nivelExperiencia, categoria);
        examenRepo.save(perfil);

        // Asignar juegos según categoría
        List<JuegoCatalogo> juegosAsignados = catalogoRepo.findByCategoria(categoria);
        for (JuegoCatalogo j : juegosAsignados) {
            // Excluir Code Runner e infantil
            if (j.getNombre().equals("Code Runner") && categoria.equals("infantil")) continue;
            if (j.getNombre().equals("Crucigrama") && categoria.equals("infantil")) continue;
            juegoUsuarioRepo.save(new JuegoUsuario(usuario, j));
        }

        return "redirect:/menu";
    }

    // Toggle activar/desactivar juego desde el menú
    @PostMapping("/juegos/toggle/{juegoId}")
    @ResponseBody
    public java.util.Map<String, Object> toggleJuego(@PathVariable Long juegoId, Authentication auth) {
        try {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
            var juegoUsuario = juegoUsuarioRepo.findByUsuarioAndJuegoId(usuario, juegoId);
            if (juegoUsuario.isPresent()) {
                JuegoUsuario ju = juegoUsuario.get();
                ju.setActivo(!ju.isActivo());
                juegoUsuarioRepo.save(ju);
                // Forzar flush para asegurar que se guarda
                juegoUsuarioRepo.flush();
                return java.util.Map.of("activo", ju.isActivo());
            }
            // Si no encuentra por juegoId, buscar todos y filtrar
            List<JuegoUsuario> todos = juegoUsuarioRepo.findByUsuario(usuario);
            for (JuegoUsuario ju : todos) {
                if (ju.getJuego().getId().equals(juegoId)) {
                    ju.setActivo(!ju.isActivo());
                    juegoUsuarioRepo.save(ju);
                    juegoUsuarioRepo.flush();
                    return java.util.Map.of("activo", ju.isActivo());
                }
            }
            return java.util.Map.of("error", "no encontrado", "juegoId", juegoId);
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    private String determinarCategoria(int edad) {
        if (edad <= 12) return "infantil";
        if (edad <= 25) return "juvenil";
        return "adulto";
    }
}
