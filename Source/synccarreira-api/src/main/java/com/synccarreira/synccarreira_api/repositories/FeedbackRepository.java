package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Feedback> findByPsychologistIdOrderByCreatedAtDesc(Long psychologistId);

    long countByStudentId(Long studentId);
}
