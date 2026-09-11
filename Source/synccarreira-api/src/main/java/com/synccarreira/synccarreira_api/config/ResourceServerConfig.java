package com.synccarreira.synccarreira_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

	@Value("${cors.origins}")
	private String corsOrigins;

	@Bean
	@Profile("test")
	@Order(1)
	public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {

		http.securityMatcher(PathRequest.toH2Console()).csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		return http.build();
	}

	@Bean
	@Order(3)
	public SecurityFilterChain rsSecurityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
				.requestMatchers(HttpMethod.POST, "/users/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/users/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/users/**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/users/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/trails").permitAll()
				.requestMatchers(HttpMethod.POST, "/trails/*/can-access").hasRole("USER")
				.requestMatchers(HttpMethod.GET, "/trails/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/trails/**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/trails/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/questions/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/questions/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/questions/**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/questions/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/psychologists/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/psychologists/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/psychologists/**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/psychologists/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/students/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/students/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/students/**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/students/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/answers/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/answers/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/answers/**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/answers/**").permitAll());
		http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(Customizer.withDefaults()));
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		grantedAuthoritiesConverter.setAuthorityPrefix("");

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		String[] origins = corsOrigins.split(",");

		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOriginPatterns(Arrays.asList(origins));
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	CorsFilter corsFilter() {
	    return new CorsFilter(corsConfigurationSource());
	}
}
