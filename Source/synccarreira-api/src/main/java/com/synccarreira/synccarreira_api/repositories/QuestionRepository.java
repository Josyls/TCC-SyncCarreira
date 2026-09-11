package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Answer;
import com.synccarreira.synccarreira_api.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTrailId(Long trailId);

    List<Question> findByPsychologistId(Long psychologist);

    long countByTrailId(Long trailId);

    @Query(value = "SELECT q.id FROM Question q WHERE q.id IN :answerIds")
    List<Long> findByAnswers(@Param("answerIds") List<Long> answerIds);
}
