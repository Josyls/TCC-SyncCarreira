package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Account;
import com.synccarreira.synccarreira_api.entities.Psychologist;
import com.synccarreira.synccarreira_api.entities.Student;
import com.synccarreira.synccarreira_api.entities.User;

import java.time.Instant;
import java.time.LocalDate;

/** Visão do administrador sobre um usuário gerenciado (RF-02). */
public record ManagedUserDTO(
        Long id,
        String name,
        String email,
        String perfil,          // ALUNO | PSICOLOGA | ADMIN
        boolean active,
        String deactivationReason,
        String cpf,
        Long institutionId,
        String institutionName,
        Long schoolClassId,
        String schoolClassName,
        // Aluno
        String schoolYear,
        String schoolType,
        // Psicóloga
        String crp,
        LocalDate contractExpirationDate,
        Instant createdAt
) {
    public static ManagedUserDTO from(User user, Account account) {
        String perfil = user.hasRole("ROLE_ADMIN") ? "ADMIN"
                : user.hasRole("ROLE_PSICOLOGA") ? "PSICOLOGA"
                : "ALUNO";

        String schoolYear = null, schoolType = null, crp = null;
        LocalDate contract = null;
        if (user instanceof Student s) {
            schoolYear = s.getSchollarYear();
            schoolType = s.getSchoolType();
        } else if (user instanceof Psychologist p) {
            crp = p.getCrp();
            contract = p.getContractExpirationDate();
        }

        return new ManagedUserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                perfil,
                account == null || account.isActive(),
                account != null ? account.getDeactivationReason() : null,
                account != null ? account.getCpf() : null,
                account != null && account.getInstitution() != null ? account.getInstitution().getId() : null,
                account != null && account.getInstitution() != null ? account.getInstitution().getLegalName() : null,
                account != null && account.getSchoolClass() != null ? account.getSchoolClass().getId() : null,
                account != null && account.getSchoolClass() != null ? account.getSchoolClass().getName() : null,
                schoolYear,
                schoolType,
                crp,
                contract,
                account != null ? account.getCreatedAt() : null
        );
    }
}
