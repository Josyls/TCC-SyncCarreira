package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.OrientationAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrientationAlertRepository extends JpaRepository<OrientationAlert, Long> {

    Optional<OrientationAlert> findByJourneyId(Long journeyId);

    List<OrientationAlert> findByResolvedFalse();

    @Query("SELECT a FROM OrientationAlert a WHERE a.resolved = false " +
            "AND a.student.id IN (SELECT c.id FROM Account c WHERE c.institution.id = :institutionId)")
    List<OrientationAlert> findOpenByInstitution(Long institutionId);
}
