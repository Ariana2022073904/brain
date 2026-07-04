package com.braintraining.repository;

import com.braintraining.model.JuegoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JuegoCatalogoRepository extends JpaRepository<JuegoCatalogo, Long> {
    @Query("SELECT j FROM JuegoCatalogo j WHERE j.categoriaEdad = 'todos' OR j.categoriaEdad LIKE %:categoria%")
    List<JuegoCatalogo> findByCategoria(String categoria);
}
