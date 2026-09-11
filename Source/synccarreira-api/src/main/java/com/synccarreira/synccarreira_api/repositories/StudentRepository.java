package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
