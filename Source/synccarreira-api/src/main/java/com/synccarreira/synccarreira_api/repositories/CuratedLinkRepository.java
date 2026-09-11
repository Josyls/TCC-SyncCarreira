package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.CuratedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CuratedLinkRepository extends JpaRepository<CuratedLink, Long> {

    List<CuratedLink> findByPsychologistId(Long psychologistId);

    @Query("SELECT l FROM CuratedLink l WHERE l.active = true " +
            "AND (l.institution IS NULL OR l.institution.id = :institutionId)")
    List<CuratedLink> findActiveForInstitution(Long institutionId);

    List<CuratedLink> findByActiveTrue();
}
