package com.braintraining.repository;

import com.braintraining.model.ExamenPerfil;
import com.braintraining.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExamenPerfilRepository extends JpaRepository<ExamenPerfil, Long> {
    Optional<ExamenPerfil> findByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);
}
