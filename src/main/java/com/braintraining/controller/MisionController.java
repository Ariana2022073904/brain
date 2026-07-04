package com.braintraining.controller;

import com.braintraining.model.*;
import com.braintraining.repository.*;
import com.braintraining.service.RecordService;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.braintraining.model.Record;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MisionController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private RecordService recordService;
    @Autowired private MisionRepository misionRepository;
    @Autowired private MisionCompletadaRepository completadaRepository;

    @Value("${openrouter.api.key:}")
    private String openrouterApiKey;

    // ── Página de misiones ───────────────────────────────────────
    @GetMapping("/misiones")
    public String misiones(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        LocalDate hoy   = LocalDate.now();

        Optional<Mision> misionOpt = misionRepository.findByUsuarioAndFecha(usuario, hoy);

        List<String> items = new ArrayList<>();
        Set<Integer> completadas = new HashSet<>();

        if (misionOpt.isPresent()) {
            items = parsearItems(misionOpt.get().getContenido());
            completadas = completadaRepository.findByUsuarioAndFecha(usuario, hoy)
                .stream().map(MisionCompletada::getMisionIndex)
                .collect(Collectors.toSet());
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("items", items);
        model.addAttribute("completadas", completadas);
        model.addAttribute("hoy", hoy.toString());
        model.addAttribute("tieneMision", misionOpt.isPresent());
        model.addAttribute("totalItems", items.size());
        model.addAttribute("totalCompletadas", completadas.size());
        return "misiones";
    }

    // ── Generar misiones del día vía AJAX ────────────────────────
    @PostMapping("/api/misiones/generar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generar(Authentication auth) {
        try {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
            LocalDate hoy   = LocalDate.now();

            // Si ya tiene misiones hoy, devolverlas
            Optional<Mision> existing = misionRepository.findByUsuarioAndFecha(usuario, hoy);
            if (existing.isPresent()) {
                List<String> items = parsearItems(existing.get().getContenido());
                Set<Integer> comp  = completadaRepository.findByUsuarioAndFecha(usuario, hoy)
                    .stream().map(MisionCompletada::getMisionIndex).collect(Collectors.toSet());
                return ResponseEntity.ok(Map.of("status","ok","items",items,"completadas",comp));
            }

            // Generar con IA
            List<Record> records = recordService.getRecordsDelUsuario(usuario);
            String prompt   = buildPrompt(usuario, records);
            String contenido = callOpenRouter(prompt);
            if (contenido == null || contenido.isBlank()) {
                contenido = generarMisionesPorDefecto(records);
            }

            Mision mision = new Mision(usuario, hoy, contenido);
            misionRepository.save(mision);

            List<String> items = parsearItems(contenido);
            return ResponseEntity.ok(Map.of("status","ok","items",items,"completadas", new HashSet<>()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status","error","msg", e.getMessage()));
        }
    }

    // ── Marcar/desmarcar misión ──────────────────────────────────
    @PostMapping("/api/misiones/toggle")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> toggle(
            @RequestBody Map<String, Object> payload,
            Authentication auth) {
        try {
            Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
            LocalDate hoy   = LocalDate.now();
            int idx = (int) payload.get("index");

            Optional<MisionCompletada> existing =
                completadaRepository.findByUsuarioAndFechaAndMisionIndex(usuario, hoy, idx);

            if (existing.isPresent()) {
                completadaRepository.deleteByUsuarioAndFechaAndMisionIndex(usuario, hoy, idx);
                return ResponseEntity.ok(Map.of("status","ok","completada", false));
            } else {
                completadaRepository.save(new MisionCompletada(usuario, hoy, idx));
                return ResponseEntity.ok(Map.of("status","ok","completada", true));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status","error","msg", e.getMessage()));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────
    private List<String> parsearItems(String contenido) {
        // Cada línea no vacía que empiece con - • * o número es una misión
        return Arrays.stream(contenido.split("\n"))
            .map(String::trim)
            .filter(l -> !l.isBlank())
            .filter(l -> l.matches("^[-•*\\d].*"))
            .map(l -> l.replaceAll("^[-•*\\d.)+\\s]+", "").trim())
            .filter(l -> !l.isBlank())
            .collect(Collectors.toList());
    }

    private String buildPrompt(Usuario usuario, List<Record> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un entrenador cognitivo. Genera EXACTAMENTE 5 misiones diarias concretas ")
          .append("para este jugador. Responde SOLO con una lista de 5 ítems, uno por línea, ")
          .append("comenzando cada uno con '- '. Sin títulos, sin secciones, sin texto extra.\n\n");
        sb.append("JUGADOR: ").append(usuario.getUsername())
          .append(", ").append(usuario.getEdad()).append(" años\n");

        if (!records.isEmpty()) {
            sb.append("RÉCORDS:\n");
            for (Record r : records) {
                sb.append("- ").append(r.getJuego())
                  .append(" (").append(r.getDificultad()).append(")")
                  .append(": puntaje ").append(r.getPuntaje()).append("\n");
            }
        } else {
            sb.append("Usuario nuevo sin récords.\n");
        }

        sb.append("\nJUEGOS DISPONIBLES: Chimp Test (memoria), Torre de Hanoi (lógica), Memoria Cartas (visual)\n");
        sb.append("\nEjemplo de formato correcto:\n");
        sb.append("- Juega 3 partidas de Chimp Test intentando superar el nivel 6\n");
        sb.append("- Completa Torre de Hanoi en modo Fácil con menos de 9 movimientos\n");
        sb.append("- Termina Memoria Cartas Medio en menos de 20 intentos\n");
        sb.append("- Repite Chimp Test hasta memorizar 5 números sin error\n");
        sb.append("- Anota tu mejor puntaje del día y compáralo con ayer\n");

        return sb.toString();
    }

    private String callOpenRouter(String prompt) throws Exception {
        if (openrouterApiKey == null || openrouterApiKey.isBlank()) {
            throw new IllegalStateException("API key de OpenRouter no configurada.");
        }

        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
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
        body.put("max_tokens", 400);
        body.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://openrouter.ai/api/v1/chat/completions", request, Map.class
        );

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
    
    private String generarMisionesPorDefecto(List<Record> records) {
            if (records.isEmpty()) {
                return "- Juega tu primera partida de Chimp Test\n" +
                       "- Completa Torre de Hanoi en modo Fácil\n" +
                       "- Juega Memoria Cartas en modo Fácil\n" +
                       "- Repite Chimp Test e intenta llegar al nivel 5\n" +
                       "- Intenta completar Torre de Hanoi en menos de 10 movimientos";
            }
            return "- Juega 3 partidas de Chimp Test e intenta superar tu récord\n" +
                   "- Completa Torre de Hanoi Medio en el mínimo de movimientos\n" +
                   "- Termina Memoria Cartas Medio en menos de 20 intentos\n" +
                   "- Repite el juego donde tengas menor puntaje\n" +
                   "- Intenta mejorar tu mejor marca en cualquier juego";
        }
}
