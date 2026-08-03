package com.zenticode.medical.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Configura la firma y validación de tokens JWT.
 */
@Configuration
public class JwtConfig {

    private static final int MINIMUM_SECRET_BYTES = 32;

    private final SecretKey secretKey;

    // Recibe la clave JWT codificada en Base64.
    public JwtConfig(
            @Value("${app.security.jwt.secret}")
            final String jwtSecret
    ) {
        this.secretKey = construirSecretKey(jwtSecret);
    }

    // Genera tokens firmados con HMAC SHA-256.
    @Bean
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .build();
    }

    // Valida firma, formato y fechas de los tokens.
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Construye una clave HMAC desde una cadena Base64.
    private static SecretKey construirSecretKey(
            final String jwtSecret
    ) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "La clave JWT es obligatoria."
            );
        }

        final byte[] secretBytes;

        try {
            secretBytes = Base64
                    .getDecoder()
                    .decode(jwtSecret.trim());
        } catch (IllegalArgumentException excepcion) {
            throw new IllegalStateException(
                    "La clave JWT no tiene un formato Base64 válido.",
                    excepcion
            );
        }

        if (secretBytes.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "La clave JWT debe contener al menos "
                            + MINIMUM_SECRET_BYTES
                            + " bytes aleatorios."
            );
        }

        return new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
        );
    }
}