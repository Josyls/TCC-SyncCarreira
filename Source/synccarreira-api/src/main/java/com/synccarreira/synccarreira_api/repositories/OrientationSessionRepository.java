package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.OrientationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrientationSessionRepository extends JpaRepository<OrientationSession, Long> {

    List<OrientationSession> findByPsychologistIdOrderByStartDesc(Long psychologistId);

    @Query("SELECT DISTINCT s FROM OrientationSession s JOIN s.participants p " +
            "WHERE p.student.id = :studentId ORDER BY s.start DESC")
    List<OrientationSession> findForStudent(Long studentId);
}
