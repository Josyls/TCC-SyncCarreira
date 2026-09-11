package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
}
