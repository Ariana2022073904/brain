package com.braintraining.repository;

import com.braintraining.model.JuegoUsuario;
import com.braintraining.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JuegoUsuarioRepository extends JpaRepository<JuegoUsuario, Long> {
    List<JuegoUsuario> findByUsuario(Usuario usuario);
    List<JuegoUsuario> findByUsuarioAndActivoTrue(Usuario usuario);
    Optional<JuegoUsuario> findByUsuarioAndJuegoId(Usuario usuario, Long juegoId);
    boolean existsByUsuario(Usuario usuario);
}
