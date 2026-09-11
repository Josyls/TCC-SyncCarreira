package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Psychologist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PsychologistRepository extends JpaRepository<Psychologist, Long> {

    // Verifica existência de CRP + nome para garantir unicidade na camada de serviço
    boolean existsByNameAndCrp(String name, String crp);

    // Busca por CRP — útil para autenticação ou consultas específicas
    Optional<Psychologist> findByCrp(String crp);
}
