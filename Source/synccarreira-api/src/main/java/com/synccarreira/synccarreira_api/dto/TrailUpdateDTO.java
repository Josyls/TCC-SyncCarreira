package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Trail;
import com.synccarreira.synccarreira_api.entities.enums.TrailName;
import jakarta.validation.constraints.NotNull;

public record TrailUpdateDTO(
        @NotNull(message = "Campo obrigatório")
        TrailName name,

        Integer sequentialOrder
) {

    public TrailUpdateDTO(Trail entity) {
        this(
                entity.getName(),
                entity.getSequentialOrder()
        );
    }
}