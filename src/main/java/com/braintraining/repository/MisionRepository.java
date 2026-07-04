package com.braintraining.repository;

import com.braintraining.model.Mision;
import com.braintraining.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MisionRepository extends JpaRepository<Mision, Long> {
    Optional<Mision> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
}
