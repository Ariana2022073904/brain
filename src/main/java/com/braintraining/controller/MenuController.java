package com.braintraining.controller;

import com.braintraining.model.Usuario;
import com.braintraining.model.ExamenPerfil;
import com.braintraining.model.JuegoUsuario;
import com.braintraining.model.Record;
import com.braintraining.repository.*;
import com.braintraining.service.RecordService;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MenuController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RecordService recordService;
    @Autowired private ExamenPerfilRepository examenRepo;
    @Autowired private JuegoUsuarioRepository juegoUsuarioRepo;

    @GetMapping("/menu")
    public String menu(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();

        // Admin va directo al dashboard
        if (usuario.getRol() == Usuario.Rol.admin) {
            return "redirect:/admin";
        }

        // Si no ha hecho el examen, redirigir
        if (!examenRepo.existsByUsuario(usuario)) {
            return "redirect:/examen";
        }

        List<Record> misRecords = recordService.getRecordsDelUsuario(usuario);
        List<JuegoUsuario> misJuegos = juegoUsuarioRepo.findByUsuario(usuario);
        ExamenPerfil perfil = examenRepo.findByUsuario(usuario).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("misRecords", misRecords);
        model.addAttribute("misJuegos", misJuegos);
        model.addAttribute("perfil", perfil);
        model.addAttribute("activePage", "menu");
        return "menu";
    }

    @GetMapping("/records")
    public String records(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("todos", recordService.getTodosLosRecords());
        model.addAttribute("misRecords", recordService.getRecordsDelUsuario(usuario));
        model.addAttribute("usuario", usuario);
        model.addAttribute("activePage", "records");
        return "records";
    }
}