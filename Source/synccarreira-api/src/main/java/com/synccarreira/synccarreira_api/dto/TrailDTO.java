package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Trail;
import com.synccarreira.synccarreira_api.entities.enums.TrailName;
import jakarta.validation.constraints.NotNull;

public record TrailDTO(
        Long id,

        @NotNull(message = "Campo obrigatório")
        TrailName name,

        Integer sequentialOrder
) {

    public TrailDTO(Trail entity) {
        this(
                entity.getId(),
                entity.getName(),
                entity.getSequentialOrder()
        );
    }
}