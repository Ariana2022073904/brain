package com.braintraining.controller;

import com.braintraining.model.Usuario;
import com.braintraining.service.RecordService;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/juegos")
public class GameController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RecordService recordService;

    // ── Chimp Test ──────────────────────────────────────────────
    @GetMapping("/chimp")
    public String chimpPage() {
        return "games/chimp";
    }

    // ── Torre de Hanoi ───────────────────────────────────────────
    @GetMapping("/hanoi")
    public String hanoiPage() {
        return "games/hanoi";
    }

    // ── Memoria Cartas ───────────────────────────────────────────
    @GetMapping("/cartas")
    public String cartasPage() {
        return "games/cartas";
    }

    // ── Save score (AJAX endpoint) ───────────────────────────────
    @PostMapping("/guardar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> guardarPuntaje(
            @RequestBody Map<String, Object> payload,
            Authentication auth) {

        try {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();

            String juego      = (String) payload.get("juego");
            String dificultad = (String) payload.get("dificultad");
            int puntaje       = (int) payload.get("puntaje");
            Integer tiempo    = payload.containsKey("tiempo") ? (Integer) payload.get("tiempo") : null;

            String resultado = recordService.guardarRecord(usuario, juego, dificultad, puntaje, tiempo);

            return ResponseEntity.ok(Map.of("status", "ok", "resultado", resultado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "msg", e.getMessage()));
        }
    }
}
