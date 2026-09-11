package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Journey;
import com.synccarreira.synccarreira_api.entities.enums.JourneyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey, Long> {

    List<Journey> findByStudentIdOrderByCycleDesc(Long studentId);

    Optional<Journey> findFirstByStudentIdAndStatusOrderByCycleDesc(Long studentId, JourneyStatus status);

    Optional<Journey> findFirstByStudentIdOrderByCycleDesc(Long studentId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(MAX(j.cycle), 0) FROM Journey j WHERE j.student.id = :studentId")
    int maxCycleForStudent(Long studentId);
}
