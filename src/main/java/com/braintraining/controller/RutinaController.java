package com.braintraining.controller;

import com.braintraining.model.Mision;
import com.braintraining.model.Record;
import com.braintraining.model.Usuario;
import com.braintraining.repository.MisionRepository;
import com.braintraining.service.RecordService;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RutinaController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RecordService recordService;
    @Autowired private MisionRepository misionRepository;

    @Value("${openrouter.api.key:}")
    private String openrouterApiKey;

    // ── Generar rutina diaria ────────────────────────────────────
    @PostMapping("/rutina")
    public ResponseEntity<Map<String, Object>> generarRutina(Authentication auth) {
        try {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
            List<Record> records = recordService.getRecordsDelUsuario(usuario);
            String prompt = buildPromptRutina(usuario, records);
            String rutina = callOpenRouter(prompt);
            if (rutina == null || rutina.isBlank()) rutina = generarMisionesPorDefecto(records);

            LocalDate hoy = LocalDate.now();
            if (misionRepository.findByUsuarioAndFecha(usuario, hoy).isEmpty()) {
                misionRepository.save(new Mision(usuario, hoy, rutina));
            }
            return ResponseEntity.ok(Map.of("rutina", rutina, "status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "msg", e.getMessage()));
        }
    }

    // ── IA: en qué ayuda un juego específico ────────────────────
    @PostMapping("/ayuda-juego")
    public ResponseEntity<Map<String, Object>> ayudaJuego(
            @RequestBody Map<String, String> payload,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
            List<Record> records = recordService.getRecordsDelUsuario(usuario);

            String juego    = payload.get("juego");
            String habilidad = payload.get("habilidad");

            // Récord del usuario en ese juego
            String recordInfo = records.stream()
                .filter(r -> r.getJuego().equalsIgnoreCase(juego))
                .map(r -> "puntaje " + r.getPuntaje() + " en dificultad " + r.getDificultad())
                .findFirst().orElse("sin récord aún");

            String prompt = "Eres un neurólogo y entrenador cognitivo. Explica en español, de forma breve y motivadora (máximo 5 oraciones), " +
                "en qué habilidades cognitivas específicas ayuda el juego '" + juego + "' que trabaja '" + habilidad + "'. " +
                "El jugador " + usuario.getUsername() + " (edad " + usuario.getEdad() + ") tiene " + recordInfo + " en este juego. " +
                "Al final, da UN consejo específico para mejorar su rendimiento en este juego. " +
                "Usa emojis. Responde solo el texto sin títulos ni listas.";

            String texto = callOpenRouter(prompt);
            if (texto == null || texto.isBlank()) {
                texto = "🧠 El juego <strong>" + juego + "</strong> entrena tu <strong>" + habilidad + "</strong>. " +
                    "Practicarlo regularmente fortalece las conexiones neuronales relacionadas con esta habilidad. " +
                    "💡 Consejo: intenta jugar al menos 3 veces por día para ver mejoras en 2 semanas.";
            }

            // Formatear texto
            String html = texto.replace("\n", "<br>")
                .replace("**", "<strong>").replace("**", "</strong>");

            return ResponseEntity.ok(Map.of("texto", html, "status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "msg", e.getMessage()));
        }
    }

    // ── Prompts ──────────────────────────────────────────────────
    private String buildPromptRutina(Usuario usuario, List<Record> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un entrenador cognitivo experto. Genera EXACTAMENTE 5 misiones diarias concretas ")
          .append("para este jugador. Responde SOLO con una lista de 5 ítems, uno por línea, ")
          .append("comenzando cada uno con '- '. Sin títulos, sin secciones, sin texto extra.\n\n");
        sb.append("JUGADOR: ").append(usuario.getUsername())
          .append(", ").append(usuario.getEdad()).append(" años\n");
        if (!records.isEmpty()) {
            sb.append("RÉCORDS:\n");
            for (Record r : records)
                sb.append("- ").append(r.getJuego()).append(": puntaje ").append(r.getPuntaje()).append("\n");
        } else sb.append("Usuario nuevo sin récords.\n");
        sb.append("\nJUEGOS: Chimp Test, Torre de Hanoi, Memoria Cartas, Pupiletras, Crucigrama, Buscaminas, Code Runner\n");
        sb.append("\nEjemplo:\n- Juega 3 partidas de Chimp Test intentando superar el nivel 6\n");
        return sb.toString();
    }

    private String generarMisionesPorDefecto(List<Record> records) {
        if (records.isEmpty())
            return "- Juega tu primera partida de Chimp Test\n- Completa Torre de Hanoi Fácil\n" +
                   "- Juega Memoria Cartas Fácil\n- Intenta Buscaminas Fácil\n- Repite el juego que más te gustó";
        return "- Juega 3 partidas de Chimp Test e intenta superar tu récord\n" +
               "- Completa Torre de Hanoi Medio en el mínimo de movimientos\n" +
               "- Termina Memoria Cartas Medio en menos de 20 intentos\n" +
               "- Juega una partida de Buscaminas nivel Medio\n" +
               "- Intenta mejorar tu mejor marca en cualquier juego";
    }

    // ── Llamada a OpenRouter ─────────────────────────────────────
    String callOpenRouter(String prompt) throws Exception {
        if (openrouterApiKey == null || openrouterApiKey.isBlank())
            throw new IllegalStateException("API key de OpenRouter no configurada.");

        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openrouterApiKey);
        headers.set("HTTP-Referer", "http://localhost:8081");
        headers.set("X-Title", "Brain Training Pro");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "openai/gpt-oss-20b:free");
        body.put("max_tokens", 600);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://openrouter.ai/api/v1/chat/completions", request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}