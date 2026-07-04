/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.braintraining.controller;

import com.braintraining.model.ExamenPerfil;
import com.braintraining.model.Usuario;
import com.braintraining.repository.ExamenPerfilRepository;
import com.braintraining.repository.UsuarioRepository;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
/**
 *
 * @author FAMILIA
 */
@Controller
public class PerfilController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ExamenPerfilRepository examenRepo;

    @GetMapping("/perfil")
    public String perfil(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        ExamenPerfil perfil = examenRepo.findByUsuario(usuario).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("perfil", perfil);
        model.addAttribute("activePage", "perfil");
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(Authentication auth,
                                @RequestParam String email,
                                @RequestParam int edad,
                                @RequestParam String objetivo,
                                @RequestParam String nivelExperiencia) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();

        usuario.setEmail(email);
        usuario.setEdad(edad);
        usuarioRepository.save(usuario);

        ExamenPerfil perfil = examenRepo.findByUsuario(usuario).orElseThrow();
        perfil.setObjetivo(objetivo);
        perfil.setNivelExperiencia(nivelExperiencia);
        examenRepo.save(perfil);

        return "redirect:/perfil?ok";
    }
}
