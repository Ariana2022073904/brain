package com.braintraining.controller;

import com.braintraining.model.Record;
import com.braintraining.model.Usuario;
import com.braintraining.repository.RecordRepository;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stats")
public class StatsController {

    @Autowired private RecordRepository recordRepository;
    @Autowired private UsuarioService usuarioService;

    @GetMapping
    public String statsPage(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("activePage", "stats");
        return "stats";
    }

    // ── API: progreso personal por juego ────────────────────────
    @GetMapping("/api/progreso-personal")
    @ResponseBody
    public Map<String, Object> progresoPersonal(Authentication auth) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        List<Record> todos = recordRepository.findByUsuarioOrderByJuegoAscPuntajeAsc(usuario);

        // Agrupar por juego, ordenar por fecha
        Map<String, List<Record>> porJuego = todos.stream()
            .collect(Collectors.groupingBy(Record::getJuego));

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Record>> entry : porJuego.entrySet()) {
            List<Record> recs = entry.getValue().stream()
                .sorted(Comparator.comparing(Record::getFecha))
                .collect(Collectors.toList());

            result.put(entry.getKey(), Map.of(
                "fechas", recs.stream().map(r -> r.getFecha().toLocalDate().toString()).collect(Collectors.toList()),
                "puntajes", recs.stream().map(Record::getPuntaje).collect(Collectors.toList()),
                "dificultades", recs.stream().map(Record::getDificultad).collect(Collectors.toList())
            ));
        }
        return result;
    }

    // ── API: ranking global por juego ────────────────────────────
    @GetMapping("/api/ranking-global")
    @ResponseBody
    public Map<String, Object> rankingGlobal() {
        List<Record> todos = recordRepository.findAllRecordsOrdered();

        Map<String, List<Record>> porJuego = todos.stream()
            .collect(Collectors.groupingBy(Record::getJuego));

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Record>> entry : porJuego.entrySet()) {
            // Top 10 por juego
            List<Record> top = entry.getValue().stream()
                .sorted(Comparator.comparingInt(Record::getPuntaje))
                .limit(10)
                .collect(Collectors.toList());

            result.put(entry.getKey(), Map.of(
                "jugadores", top.stream().map(r -> r.getUsuario().getUsername()).collect(Collectors.toList()),
                "puntajes",  top.stream().map(Record::getPuntaje).collect(Collectors.toList()),
                "edades",    top.stream().map(r -> r.getUsuario().getEdad()).collect(Collectors.toList())
            ));
        }
        return result;
    }

    // ── API: distribución por edad ───────────────────────────────
    @GetMapping("/api/por-edad")
    @ResponseBody
    public Map<String, Object> porEdad() {
        List<Record> todos = recordRepository.findAllRecordsOrdered();

        // Agrupar en rangos de edad
        Map<String, List<Integer>> grupos = new LinkedHashMap<>();
        grupos.put("5-12",  new ArrayList<>());
        grupos.put("13-17", new ArrayList<>());
        grupos.put("18-25", new ArrayList<>());
        grupos.put("26-35", new ArrayList<>());
        grupos.put("36-50", new ArrayList<>());
        grupos.put("50+",   new ArrayList<>());

        for (Record r : todos) {
            int edad = r.getUsuario().getEdad();
            String grupo = edad <= 12 ? "5-12" : edad <= 17 ? "13-17" :
                           edad <= 25 ? "18-25" : edad <= 35 ? "26-35" :
                           edad <= 50 ? "36-50" : "50+";
            grupos.get(grupo).add(r.getPuntaje());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", new ArrayList<>(grupos.keySet()));
        result.put("promedios", grupos.values().stream()
            .map(list -> list.isEmpty() ? 0 :
                (int) list.stream().mapToInt(Integer::intValue).average().orElse(0))
            .collect(Collectors.toList()));
        result.put("cantidad", grupos.values().stream()
            .map(List::size).collect(Collectors.toList()));
        return result;
    }

    // ── API: agilidad cerebral (score compuesto) ─────────────────
    @GetMapping("/api/agilidad")
    @ResponseBody
    public Map<String, Object> agilidad(Authentication auth) {
        List<Record> todos = recordRepository.findAllRecordsOrdered();
        List<Usuario> usuarios = usuarioService.findAll();

        List<Map<String, Object>> ranking = new ArrayList<>();

        for (Usuario u : usuarios) {
            List<Record> misRecs = todos.stream()
                .filter(r -> r.getUsuario().getId().equals(u.getId()))
                .collect(Collectors.toList());

            if (misRecs.isEmpty()) continue;

            // Score compuesto: Chimp (nivel alto = bueno), Hanoi (movimientos bajos = bueno), Cartas (intentos bajos = bueno)
            double score = 0;
            int count = 0;

            OptionalDouble chimp = misRecs.stream().filter(r -> r.getJuego().equals("Chimp"))
                .mapToInt(Record::getPuntaje).average();
            OptionalDouble hanoi = misRecs.stream().filter(r -> r.getJuego().equals("Hanoi"))
                .mapToInt(Record::getPuntaje).average();
            OptionalDouble cartas = misRecs.stream().filter(r -> r.getJuego().equals("Cartas"))
                .mapToInt(Record::getPuntaje).average();

            if (chimp.isPresent())  { score += chimp.getAsDouble() * 10; count++; }  // más alto = mejor
            if (hanoi.isPresent())  { score += Math.max(0, 100 - hanoi.getAsDouble()); count++; } // menos movimientos = mejor
            if (cartas.isPresent()) { score += Math.max(0, 100 - cartas.getAsDouble()); count++; } // menos intentos = mejor

            if (count > 0) {
                ranking.add(Map.of(
                    "usuario", u.getUsername(),
                    "edad", u.getEdad(),
                    "score", (int)(score / count),
                    "partidas", misRecs.size()
                ));
            }
        }

        ranking.sort((a, b) -> (int)b.get("score") - (int)a.get("score"));

        return Map.of(
            "ranking", ranking,
            "labels",  ranking.stream().map(m -> m.get("usuario")).collect(Collectors.toList()),
            "scores",  ranking.stream().map(m -> m.get("score")).collect(Collectors.toList()),
            "edades",  ranking.stream().map(m -> m.get("edad")).collect(Collectors.toList())
        );
    }
}
