package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Trail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrailRepository extends JpaRepository<Trail, Long> {

    // Busca a trilha anterior (ordem - 1) para verificar se está concluída
    @Query("SELECT t FROM Trail t WHERE t.sequentialOrder = :order")
    Optional<Trail> findBySequentialOrder(@Param("order") Integer order);
}
