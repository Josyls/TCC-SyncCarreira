package com.synccarreira.synccarreira_api.security;

import com.synccarreira.synccarreira_api.repositories.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Bloqueia (HTTP 403) requisições autenticadas de contas desativadas (RF-02).
 *
 * <p>Necessário porque o provedor de autenticação legado
 * ({@code CustomPasswordAuthenticationProvider}) não pode ser alterado e ainda
 * emitiria token para uma conta desativada — este filtro impede que esse token
 * acesse qualquer recurso protegido pela cadeia de segurança nova.</p>
 */
@Component
public class AccountStatusFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;

    public AccountStatusFilter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            String username = jwt.getClaimAsString("username");
            if (username == null || username.isBlank()) {
                username = jwt.getSubject();
            }

            if (username != null && !username.isBlank()) {
                String email = username;
                boolean blocked = accountRepository.findByUserEmail(email)
                        .map(account -> !account.isActive())
                        .orElse(false);

                if (blocked) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(String.format(
                            "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"%s\",\"path\":\"%s\"}",
                            Instant.now(),
                            "Conta desativada. Procure o administrador.",
                            request.getRequestURI()));
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
