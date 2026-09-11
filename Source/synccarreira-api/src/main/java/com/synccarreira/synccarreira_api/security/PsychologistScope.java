package com.synccarreira.synccarreira_api.security;

import com.synccarreira.synccarreira_api.entities.Account;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.repositories.AccountRepository;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Define o conjunto de alunos que a psicóloga logada pode ver/gerenciar —
 * base do isolamento por instituição (RNF-08, RN-03).
 *
 * <p>Escopo = alunos (ROLE_USER) cuja conta está vinculada à mesma instituição
 * da psicóloga. Administrador não tem restrição.</p>
 */
@Component
public class PsychologistScope {

    private final CurrentUser currentUser;
    private final AccountRepository accountRepository;

    public PsychologistScope(CurrentUser currentUser, AccountRepository accountRepository) {
        this.currentUser = currentUser;
        this.accountRepository = accountRepository;
    }

    /** Instituição da psicóloga logada (nula se admin ou sem vínculo). */
    @Transactional(readOnly = true)
    public Long institutionId() {
        return currentUser.account()
                .map(a -> a.getInstitution() != null ? a.getInstitution().getId() : null)
                .orElse(null);
    }

    /** Alunos no escopo da psicóloga logada. */
    @Transactional(readOnly = true)
    public List<User> studentsInScope() {
        Long instId = institutionId();
        if (currentUser.isAdmin()) {
            return accountRepository.findAll().stream()
                    .map(Account::getUser)
                    .filter(u -> u != null && u.hasRole("ROLE_USER"))
                    .toList();
        }
        if (instId == null) {
            return List.of();
        }
        return accountRepository.findByInstitutionId(instId).stream()
                .map(Account::getUser)
                .filter(u -> u != null && u.hasRole("ROLE_USER"))
                .toList();
    }

    @Transactional(readOnly = true)
    public void assertStudentInScope(Long studentId) {
        boolean ok = studentsInScope().stream().anyMatch(u -> u.getId().equals(studentId));
        if (!ok) {
            throw new ForbiddenOperationException(
                    "Este aluno não pertence à sua instituição (RNF-08).");
        }
    }
}
