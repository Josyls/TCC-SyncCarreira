package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT a FROM Answer a " +
            "JOIN FETCH a.questionOption qo " +
            "JOIN FETCH qo.question q " +
            "WHERE a.student.id = :studentId " +
            "AND q.trail.id = :trailId")
    List<Answer> findByStudentAndTrail(Long studentId, Long trailId);

    // AnswerRepository.java
    @Query("SELECT a FROM Answer a JOIN FETCH a.questionOption WHERE a.student.id = :studentId")
    List<Answer> findByStudentId(Long studentId);
}
