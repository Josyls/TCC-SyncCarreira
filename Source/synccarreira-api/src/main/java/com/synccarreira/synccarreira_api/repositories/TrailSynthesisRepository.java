package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.TrailSynthesis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrailSynthesisRepository extends JpaRepository<TrailSynthesis, Long> {

    Optional<TrailSynthesis> findByJourneyIdAndTrailId(Long journeyId, Long trailId);

    List<TrailSynthesis> findByJourneyId(Long journeyId);
}
