package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.JourneyTrail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JourneyTrailRepository extends JpaRepository<JourneyTrail, Long> {

    List<JourneyTrail> findByJourneyId(Long journeyId);

    Optional<JourneyTrail> findByJourneyIdAndTrailId(Long journeyId, Long trailId);
}
