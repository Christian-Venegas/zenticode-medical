package com.zenticode.medical.shared.config;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;

/**
 * Configura el acceso del frontend autorizado a la API.
 */
@Configuration
public class CorsConfig {

    private static final String ORIGEN_FRONTEND_LOCAL =
            "http://localhost:5173";

    // Registra CORS antes de los filtros de seguridad.
    @Bean
    public FilterRegistrationBean<CorsFilter>
    corsFilterRegistration() {
        final CorsConfiguration configuracion =
                construirConfiguracion();

        final UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration(
                "/api/**",
                configuracion
        );

        final FilterRegistrationBean<CorsFilter> registro =
                new FilterRegistrationBean<>(
                        new CorsFilter(fuente)
                );

        registro.setName(
                "zenticodeCorsFilter"
        );

        registro.setOrder(
                Ordered.HIGHEST_PRECEDENCE
        );

        registro.setDispatcherTypes(
                EnumSet.of(
                        DispatcherType.REQUEST,
                        DispatcherType.ASYNC,
                        DispatcherType.ERROR
                )
        );

        return registro;
    }

    // Define únicamente orígenes, métodos y cabeceras necesarias.
    private static CorsConfiguration construirConfiguracion() {
        final CorsConfiguration configuracion =
                new CorsConfiguration();

        configuracion.setAllowedOrigins(
                List.of(
                        ORIGEN_FRONTEND_LOCAL
                )
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
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );

        configuracion.setExposedHeaders(
                List.of(
                        "Content-Disposition",
                        "Content-Length",
                        "Location"
                )
        );

        configuracion.setAllowCredentials(
                false
        );

        configuracion.setMaxAge(
                Duration.ofHours(1)
        );

        return configuracion;
    }
}