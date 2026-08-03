package com.zenticode.medical.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configura los orígenes autorizados para consumir la API.
 */
@Configuration
public class CorsConfig {

    private final List<String> origenesPermitidos;

    // Lee los dominios permitidos desde el entorno.
    public CorsConfig(
            @Value(
                    "${CORS_ALLOWED_ORIGINS:"
                            + "http://localhost:5173}"
            )
            final String origenes
    ) {
        this.origenesPermitidos =
                Arrays.stream(
                                origenes.split(",")
                        )
                        .map(String::trim)
                        .filter(
                                origen ->
                                        !origen.isBlank()
                        )
                        .distinct()
                        .toList();

        if (origenesPermitidos.isEmpty()) {
            throw new IllegalStateException(
                    "Debe existir al menos "
                            + "un origen CORS permitido."
            );
        }
    }

    // Integra CORS con Spring Security.
    @Bean
    public UrlBasedCorsConfigurationSource
    corsConfigurationSource() {
        final CorsConfiguration configuracion =
                new CorsConfiguration();

        configuracion.setAllowedOrigins(
                origenesPermitidos
        );

        configuracion.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuracion.setAllowedHeaders(
                List.of(
                        "Accept",
                        "Authorization",
                        "Content-Type",
                        "Origin",
                        "X-Requested-With"
                )
        );

        configuracion.setExposedHeaders(
                List.of(
                        "Location"
                )
        );

        // La autenticación utiliza JWT, no cookies.
        configuracion.setAllowCredentials(false);

        // Reduce peticiones preflight repetidas.
        configuracion.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration(
                "/**",
                configuracion
        );

        return fuente;
    }
}