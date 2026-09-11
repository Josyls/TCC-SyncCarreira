package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.JourneyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JourneyAnswerRepository extends JpaRepository<JourneyAnswer, Long> {

    Optional<JourneyAnswer> findByJourneyIdAndQuestionId(Long journeyId, Long questionId);

    List<JourneyAnswer> findByJourneyId(Long journeyId);

    @Query("SELECT a FROM JourneyAnswer a WHERE a.journey.id = :journeyId AND a.question.trail.id = :trailId")
    List<JourneyAnswer> findByJourneyAndTrail(Long journeyId, Long trailId);

    @Query("SELECT COUNT(a) FROM JourneyAnswer a WHERE a.journey.id = :journeyId AND a.question.trail.id = :trailId")
    long countByJourneyAndTrail(Long journeyId, Long trailId);
}
