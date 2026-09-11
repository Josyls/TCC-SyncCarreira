package com.synccarreira.synccarreira_api.config;

import com.synccarreira.synccarreira_api.security.AccountStatusFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Cadeia de segurança das funcionalidades novas (Blocos 1–6).
 *
 * <p>Existe porque o {@code ResourceServerConfig} legado não pode ser editado e
 * ele nega (403) qualquer rota que não esteja na sua lista. Esta cadeia tem
 * precedência ({@code @Order(0)}) sobre a legada ({@code @Order(3)}) e cobre
 * tanto os prefixos novos quanto os endpoints legados sensíveis, passando a
 * exigir autenticação e, onde aplicável, papel específico.</p>
 *
 * <p>Não intercepta {@code /oauth2/**}, {@code /.well-known/**},
 * {@code /swagger-ui/**} nem {@code /v3/api-docs/**} — essas continuam com as
 * cadeias legadas.</p>
 */
@Configuration
public class SecurityConfigV2 {

    /** Prefixos cobertos por esta cadeia (novos + legados sensíveis). */
    static final String[] MATCHED_PATHS = {
            // Página de erro do Spring — precisa passar por esta cadeia para que
            // um 404/500 não seja mascarado como 403 pela cadeia legada.
            "/error",
            // ---- Funcionalidades novas ----
            "/institutions/**",
            "/classes/**",
            "/admin/**",
            "/journeys/**",
            "/student-profile/**",
            "/syntheses/**",
            "/career-catalog/**",
            "/career-results/**",
            "/curated-links/**",
            "/psychologist-panel/**",
            "/reports/**",
            "/appointments/**",
            "/feedbacks/**",
            // ---- Endpoints legados sensíveis (retrofit de autorização) ----
            "/users/**",
            "/students/**",
            "/psychologists/**",
            "/trails/**",
            "/questions/**",
            "/answers/**"
    };

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain apiV2SecurityFilterChain(HttpSecurity http,
                                                       AccountStatusFilter accountStatusFilter) throws Exception {

        http.securityMatcher(MATCHED_PATHS);

        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth
                // Página de erro sempre acessível (renderiza o status real).
                .requestMatchers("/error").permitAll()

                // Auto-cadastro público — preserva o fluxo atual do frontend.
                .requestMatchers(HttpMethod.POST, "/users", "/students", "/psychologists").permitAll()

                // Escrita no catálogo de trilhas/perguntas: admin ou psicóloga (RN-06).
                .requestMatchers(HttpMethod.POST, "/trails", "/questions").hasAnyRole("ADMIN", "PSICOLOGA")
                .requestMatchers(HttpMethod.PUT, "/trails/**", "/questions/**").hasAnyRole("ADMIN", "PSICOLOGA")
                .requestMatchers(HttpMethod.DELETE, "/trails/**", "/questions/**").hasAnyRole("ADMIN", "PSICOLOGA")

                // Gestão de contas legada: só admin.
                .requestMatchers(HttpMethod.DELETE, "/users/**", "/students/**", "/psychologists/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/psychologists/**").hasAnyRole("ADMIN", "PSICOLOGA")

                // Demais operações protegidas por prefixo novo usam @PreAuthorize no controller.
                .anyRequest().authenticated());

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        http.addFilterBefore(accountStatusFilter, AuthorizationFilter.class);

        return http.build();
    }
}
