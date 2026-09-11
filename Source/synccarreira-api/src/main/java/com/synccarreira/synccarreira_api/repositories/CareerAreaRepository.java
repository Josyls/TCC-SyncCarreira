package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.CareerArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerAreaRepository extends JpaRepository<CareerArea, Long> {

    List<CareerArea> findByActiveTrue();

    List<CareerArea> findByAreaAndActiveTrue(String area);
}
