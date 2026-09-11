package com.synccarreira.synccarreira_api.dto.psicologa;

import com.synccarreira.synccarreira_api.entities.CuratedLink;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** DTOs de curadoria de links (RF-06, RN-05). */
public final class CuratedLinkDTOs {

    private CuratedLinkDTOs() {
    }

    public record View(
            Long id, String title, String url, String description, String category,
            boolean active, Long institutionId, String institutionName
    ) {
        public View(CuratedLink e) {
            this(e.getId(), e.getTitle(), e.getUrl(), e.getDescription(), e.getCategory(),
                    Boolean.TRUE.equals(e.getActive()),
                    e.getInstitution() != null ? e.getInstitution().getId() : null,
                    e.getInstitution() != null ? e.getInstitution().getLegalName() : null);
        }
    }

    public record InstitutionOption(Long id, String name) {}

    public record Insert(
            @NotBlank(message = "Campo obrigatório") String title,
            @NotBlank(message = "Campo obrigatório") String url,
            String description,
            @NotBlank(message = "Campo obrigatório")
            @Pattern(regexp = "ENEM|PROUNI|SISU|COTAS|CURSOS|OUTRO", message = "Categoria inválida")
            String category,
            /** Nulo = link global (todas as instituições). */
            Long institutionId
    ) {
    }

    public record Update(
            @NotBlank(message = "Campo obrigatório") String title,
            @NotBlank(message = "Campo obrigatório") String url,
            String description,
            @NotBlank(message = "Campo obrigatório")
            @Pattern(regexp = "ENEM|PROUNI|SISU|COTAS|CURSOS|OUTRO", message = "Categoria inválida")
            String category,
            Long institutionId,
            @NotNull(message = "Campo obrigatório") Boolean active
    ) {
    }
}
