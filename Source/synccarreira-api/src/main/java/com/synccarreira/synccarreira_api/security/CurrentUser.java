package com.synccarreira.synccarreira_api.security;

import com.synccarreira.synccarreira_api.entities.Account;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.repositories.AccountRepository;
import com.synccarreira.synccarreira_api.repositories.UserRepository;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Resolve o usuário autenticado (e sua {@link Account}) a partir do JWT.
 * Ponto único de acesso ao "quem está logado" para os serviços novos —
 * base de RNF-01 (autorização) e RNF-08 (isolamento por instituição).
 */
@Component
public class CurrentUser {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public CurrentUser(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    /** E-mail (username) do usuário autenticado, extraído do claim {@code username} do JWT. */
    public String email() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ForbiddenOperationException("Requisição não autenticada.");
        }
        String username = jwt.getClaimAsString("username");
        if (username == null || username.isBlank()) {
            username = jwt.getSubject();
        }
        if (username == null || username.isBlank()) {
            throw new ForbiddenOperationException("Token sem identificação de usuário.");
        }
        return username;
    }

    @Transactional(readOnly = true)
    public User user() {
        User user = userRepository.findByEmail(email());
        if (user == null) {
            throw new ForbiddenOperationException("Usuário autenticado não encontrado.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Long userId() {
        return user().getId();
    }

    @Transactional(readOnly = true)
    public Optional<Account> account() {
        return accountRepository.findByUserEmail(email());
    }

    @Transactional(readOnly = true)
    public Account requireAccount() {
        return account().orElseThrow(() ->
                new ForbiddenOperationException("Conta administrativa não encontrada para o usuário."));
    }

    public boolean hasRole(String roleWithoutPrefix) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String target = "ROLE_" + roleWithoutPrefix;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> target.equals(a.getAuthority()));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isPsychologist() {
        return hasRole("PSICOLOGA");
    }

    public boolean isStudent() {
        return hasRole("USER");
    }

    /**
     * Garante que o alvo pertence à mesma instituição do usuário autenticado.
     * Admin não tem restrição de instituição. (RNF-08)
     */
    @Transactional(readOnly = true)
    public void assertSameInstitution(Long targetInstitutionId) {
        if (isAdmin()) {
            return;
        }
        Long mine = account().map(a -> a.getInstitution() != null ? a.getInstitution().getId() : null)
                .orElse(null);
        if (mine == null || !mine.equals(targetInstitutionId)) {
            throw new ForbiddenOperationException(
                    "Operação bloqueada: recurso de outra instituição (RNF-08).");
        }
    }
}
