package com.braintraining.service;

import com.braintraining.model.Record;
import com.braintraining.model.Usuario;
import com.braintraining.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    /**
     * Saves a record. For Chimp Test: puntaje = seconds (lower is better).
     * For Hanoi: puntaje = movements (lower is better).
     * Only saves if it's a new personal best.
     */
    public String guardarRecord(Usuario usuario, String juego, String dificultad, int puntaje, Integer tiempoSeg) {
        Optional<Record> existing = recordRepository.findByUsuarioAndJuegoAndDificultad(usuario, juego, dificultad);

        if (existing.isPresent()) {
            Record r = existing.get();
            if (puntaje < r.getPuntaje()) {
                r.setPuntaje(puntaje);
                r.setTiempoSeg(tiempoSeg);
                recordRepository.save(r);
                return "nuevo_record";
            }
            return "no_mejora";
        } else {
            Record nuevo = new Record(usuario, juego, dificultad, puntaje, tiempoSeg);
            recordRepository.save(nuevo);
            return "primer_record";
        }
    }

    public List<Record> getRecordsDelUsuario(Usuario usuario) {
        return recordRepository.findByUsuarioOrderByJuegoAscPuntajeAsc(usuario);
    }

    public List<Record> getTodosLosRecords() {
        return recordRepository.findAllRecordsOrdered();
    }

    public List<Record> getRecordsPorJuego(String juego) {
        return recordRepository.findByJuegoOrderByPuntajeAsc(juego);
    }

    public long contarRecords() {
        return recordRepository.countAllRecords();
    }

    public void eliminar(Long id) {
        recordRepository.deleteById(id);
    }
}
    