package com.zenticode.medical.auth.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Información segura devuelta después del inicio de sesión.
 */
public record LoginResponse(

        Long idUsuarios,

        Long idConsultorios,

        String correo,

        String nombres,

        String apellidos,

        List<String> roles,

        OffsetDateTime fechaAcceso,

        String accessToken,

        String tokenType,

        Instant expiresAt

) {

    // Protege las colecciones y valida los datos del token.
    public LoginResponse {
        roles = roles == null
                ? List.of()
                : List.copyOf(roles);

        if (accessToken != null
                && accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de acceso no puede estar vacío."
            );
        }

        if (tokenType != null
                && tokenType.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo del token no puede estar vacío."
            );
        }

        // Si existe token, sus metadatos también son obligatorios.
        if (accessToken != null
                && (tokenType == null || expiresAt == null)) {
            throw new IllegalArgumentException(
                    "Los datos del token están incompletos."
            );
        }
    }

    // Mantiene compatibilidad mientras conectamos JwtService.
    public LoginResponse(
            final Long idUsuarios,
            final Long idConsultorios,
            final String correo,
            final String nombres,
            final String apellidos,
            final List<String> roles,
            final OffsetDateTime fechaAcceso
    ) {
        this(
                idUsuarios,
                idConsultorios,
                correo,
                nombres,
                apellidos,
                roles,
                fechaAcceso,
                null,
                null,
                null
        );
    }
}