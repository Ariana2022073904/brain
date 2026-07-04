package com.braintraining.repository;

import com.braintraining.model.Record;
import com.braintraining.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByUsuarioOrderByJuegoAscPuntajeAsc(Usuario usuario);

    List<Record> findAllByOrderByJuegoAscPuntajeAsc();

    // Top records per game (global leaderboard)
    @Query("SELECT r FROM Record r ORDER BY r.juego ASC, r.puntaje ASC")
    List<Record> findAllRecordsOrdered();

    // Best record for a user in a specific game+difficulty
    Optional<Record> findByUsuarioAndJuegoAndDificultad(Usuario usuario, String juego, String dificultad);

    // Stats for admin
    @Query("SELECT COUNT(r) FROM Record r")
    long countAllRecords();

    List<Record> findByJuegoOrderByPuntajeAsc(String juego);
}
