package com.braintraining.controller;

import com.braintraining.service.RecordService;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RecordService recordService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("records", recordService.getTodosLosRecords());
        model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
        model.addAttribute("totalRecords", recordService.contarRecords());
        return "admin/dashboard";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return "redirect:/admin";
    }

    @PostMapping("/records/eliminar/{id}")
    public String eliminarRecord(@PathVariable Long id) {
        recordService.eliminar(id);
        return "redirect:/admin";
    }
}
