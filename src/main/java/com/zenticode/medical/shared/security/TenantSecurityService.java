package com.zenticode.medical.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Verifica el aislamiento entre consultorios.
 */
@Service
public class TenantSecurityService {

    private static final String CLAIM_ID_CONSULTORIOS =
            "idConsultorios";

    // Comprueba que el JWT pertenezca al consultorio solicitado.
    public boolean perteneceAlConsultorio(
            final Authentication authentication,
            final Long idConsultorios
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || idConsultorios == null
                || idConsultorios <= 0) {
            return false;
        }

        if (!(authentication
                instanceof JwtAuthenticationToken jwtToken)) {
            return false;
        }

        final Object claimConsultorio =
                jwtToken
                        .getToken()
                        .getClaims()
                        .get(CLAIM_ID_CONSULTORIOS);

        final Long idConsultoriosToken =
                convertirALong(claimConsultorio);

        return Objects.equals(
                idConsultoriosToken,
                idConsultorios
        );
    }

    // Comprueba además que el usuario tenga alguno de los roles.
    public boolean perteneceAlConsultorioConRol(
            final Authentication authentication,
            final Long idConsultorios,
            final String... rolesPermitidos
    ) {
        if (!perteneceAlConsultorio(
                authentication,
                idConsultorios
        )) {
            return false;
        }

        if (rolesPermitidos == null
                || rolesPermitidos.length == 0) {
            return false;
        }

        for (final String rol : rolesPermitidos) {
            if (tieneRol(authentication, rol)) {
                return true;
            }
        }

        return false;
    }

    // Comprueba una autoridad ROLE_* del usuario.
    private static boolean tieneRol(
            final Authentication authentication,
            final String rol
    ) {
        if (rol == null || rol.isBlank()) {
            return false;
        }

        final String autoridadEsperada =
                rol.startsWith("ROLE_")
                        ? rol.trim()
                        : "ROLE_" + rol.trim();

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        autoridad -> autoridadEsperada.equals(
                                autoridad.getAuthority()
                        )
                );
    }

    // Convierte el claim numérico de forma defensiva.
    private static Long convertirALong(
            final Object valor
    ) {
        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        if (valor instanceof String texto) {
            try {
                return Long.valueOf(texto.trim());
            } catch (NumberFormatException excepcion) {
                return null;
            }
        }

        return null;
    }
}