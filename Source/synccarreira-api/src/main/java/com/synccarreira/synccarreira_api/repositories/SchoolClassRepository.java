package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findByInstitutionId(Long institutionId);

    List<SchoolClass> findByPsychologistId(Long psychologistId);

    boolean existsByInstitutionId(Long institutionId);
}
