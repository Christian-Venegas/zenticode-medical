package com.zenticode.medical.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Genera tokens JWT firmados para usuarios autenticados.
 */
@Service
public class JwtService {

    private static final long DURACION_MINIMA_MINUTOS = 5;
    private static final long DURACION_MAXIMA_MINUTOS = 60;

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration duracionToken;

    // Recibe el codificador y la configuración del token.
    public JwtService(
            final JwtEncoder jwtEncoder,

            @Value(
                    "${app.security.jwt.issuer:"
                            + "zenticode-medical-api}"
            )
            final String issuer,

            @Value(
                    "${app.security.jwt."
                            + "expiration-minutes:30}"
            )
            final long expirationMinutes
    ) {
        this.jwtEncoder = Objects.requireNonNull(
                jwtEncoder,
                "El codificador JWT es obligatorio."
        );

        this.issuer = validarIssuer(issuer);

        this.duracionToken =
                validarDuracion(expirationMinutes);
    }

    // Genera un token firmado para el usuario autenticado.
    public TokenGenerado generarToken(
            final Long idUsuarios,
            final Long idConsultorios,
            final List<String> roles
    ) {
        validarIdentificador(
                idUsuarios,
                "El identificador del usuario no es válido."
        );

        validarIdentificador(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        final List<String> rolesNormalizados =
                normalizarRoles(roles);

        final Instant emitidoEn =
                Instant.now();

        final Instant expiraEn =
                emitidoEn.plus(duracionToken);

        final JwsHeader cabecera =
                JwsHeader
                        .with(MacAlgorithm.HS256)
                        .type("JWT")
                        .build();

        final JwtClaimsSet claims =
                JwtClaimsSet
                        .builder()
                        .issuer(issuer)
                        .subject(idUsuarios.toString())
                        .issuedAt(emitidoEn)
                        .expiresAt(expiraEn)
                        .claim(
                                "idConsultorios",
                                idConsultorios
                        )
                        .claim(
                                "roles",
                                rolesNormalizados
                        )
                        .build();

        final String token =
                jwtEncoder
                        .encode(
                                JwtEncoderParameters.from(
                                        cabecera,
                                        claims
                                )
                        )
                        .getTokenValue();

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "No fue posible generar el token de acceso."
            );
        }

        return new TokenGenerado(
                token,
                "Bearer",
                emitidoEn,
                expiraEn
        );
    }

    // Normaliza y protege los roles incluidos en el token.
    private static List<String> normalizarRoles(
            final List<String> roles
    ) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario debe tener al menos un rol activo."
            );
        }

        final List<String> rolesNormalizados =
                roles.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(rol -> !rol.isBlank())
                        .map(
                                rol -> rol.toUpperCase(
                                        Locale.ROOT
                                )
                        )
                        .filter(
                                rol -> rol.matches(
                                        "^[A-Z][A-Z0-9_]*$"
                                )
                        )
                        .distinct()
                        .sorted()
                        .toList();

        if (rolesNormalizados.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario debe tener al menos un rol válido."
            );
        }

        return rolesNormalizados;
    }

    // Valida el identificador del emisor.
    private static String validarIssuer(
            final String issuer
    ) {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "El emisor JWT es obligatorio."
            );
        }

        return issuer.trim();
    }

    // Limita la duración de los tokens de acceso.
    private static Duration validarDuracion(
            final long expirationMinutes
    ) {
        if (expirationMinutes < DURACION_MINIMA_MINUTOS
                || expirationMinutes > DURACION_MAXIMA_MINUTOS) {
            throw new IllegalArgumentException(
                    "La duración del token debe estar entre "
                            + DURACION_MINIMA_MINUTOS
                            + " y "
                            + DURACION_MAXIMA_MINUTOS
                            + " minutos."
            );
        }

        return Duration.ofMinutes(expirationMinutes);
    }

    // Comprueba que una clave primaria sea positiva.
    private static void validarIdentificador(
            final Long identificador,
            final String mensaje
    ) {
        if (identificador == null || identificador <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    /**
     * Resultado interno de la generación del token.
     */
    public record TokenGenerado(

            String token,

            String tipo,

            Instant emitidoEn,

            Instant expiraEn

    ) {

        // Impide construir un resultado incompleto.
        public TokenGenerado {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException(
                        "El token generado es obligatorio."
                );
            }

            if (tipo == null || tipo.isBlank()) {
                throw new IllegalArgumentException(
                        "El tipo del token es obligatorio."
                );
            }

            Objects.requireNonNull(
                    emitidoEn,
                    "La fecha de emisión es obligatoria."
            );

            Objects.requireNonNull(
                    expiraEn,
                    "La fecha de expiración es obligatoria."
            );

            if (!expiraEn.isAfter(emitidoEn)) {
                throw new IllegalArgumentException(
                        "La expiración debe ser posterior "
                                + "a la emisión."
                );
            }
        }
    }
}