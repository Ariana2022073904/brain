package com.braintraining.controller;

import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("errorMsg", "Usuario o contraseña incorrectos.");
        if (logout != null) model.addAttribute("logoutMsg", "Sesión cerrada correctamente.");
        return "login";
    }

    @GetMapping("/registro")
    public String registroPage() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam String email,
                            @RequestParam int edad,
                            Model model) {

        if (usuarioService.existeUsername(username)) {
            model.addAttribute("error", "El nombre de usuario ya está en uso.");
            return "registro";
        }
        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "El email ya está registrado.");
            return "registro";
        }
        if (edad < 5 || edad > 120) {
            model.addAttribute("error", "Ingresa una edad válida.");
            return "registro";
        }

        usuarioService.registrar(username, password, email, edad);
        return "redirect:/login?registrado=true";
    }
}
