package com.braintraining.repository;

import com.braintraining.model.MisionCompletada;
import com.braintraining.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MisionCompletadaRepository extends JpaRepository<MisionCompletada, Long> {
    List<MisionCompletada> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
    Optional<MisionCompletada> findByUsuarioAndFechaAndMisionIndex(Usuario usuario, LocalDate fecha, int index);
    void deleteByUsuarioAndFechaAndMisionIndex(Usuario usuario, LocalDate fecha, int index);
}
