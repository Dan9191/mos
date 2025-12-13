package ru.hackathon.mos.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Проверка существования пользователя.
     */
    private final UserSyncFilter userSyncFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000", "https://mos-hack.ru/"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowedHeaders(List.of(
                            "Authorization",
                            "Content-Type",
                            "X-Requested-With",
                            "Accept",
                            "Origin",
                            "Access-Control-Request-Method",
                            "Access-Control-Request-Headers"
                    ));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/templates/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/templates/**").hasAnyAuthority("ROLE_hackathon.admin", "ROLE_hackathon.manager")
                        .requestMatchers(HttpMethod.PUT, "/api/templates/**").hasAnyAuthority("ROLE_hackathon.admin", "ROLE_hackathon.manager")
                        .requestMatchers(HttpMethod.POST, "/api/applications").hasAuthority("ROLE_hackathon.user")
                        // блок операций по заявкам
                        .requestMatchers(HttpMethod.POST, "/api/applications/*/take").hasAnyAuthority("ROLE_hackathon.admin", "ROLE_hackathon.manager")
                        .requestMatchers(HttpMethod.POST, "/api/applications/*/reject").hasAnyAuthority("ROLE_hackathon.admin", "ROLE_hackathon.manager")
                        .requestMatchers(HttpMethod.POST, "/api/applications/*/accept").hasAnyAuthority("ROLE_hackathon.admin", "ROLE_hackathon.manager")
                        // тестовые запросы
                        .requestMatchers("/api/v1/events/**").hasAuthority("ROLE_hackathon.admin")
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(
                                "/api/swagger-ui.html",
                                "/api/swagger-ui/**",
                                "/api/v3/api-docs",
                                "/api/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/api/webjars/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterAfter(userSyncFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
