package com.zenticode.medical.signosvitales.controller;

import com.zenticode.medical.signosvitales.dto.SignosVitalesRequest;
import com.zenticode.medical.signosvitales.dto.SignosVitalesResponse;
import com.zenticode.medical.signosvitales.service.SignosVitalesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Expone las operaciones HTTP de signos vitales.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}"
                + "/pacientes/{idPacientes}"
)
public class SignosVitalesController {

    private final SignosVitalesService signosVitalesService;

    // Inyecta el servicio obligatorio.
    public SignosVitalesController(
            final SignosVitalesService signosVitalesService
    ) {
        this.signosVitalesService =
                Objects.requireNonNull(
                        signosVitalesService,
                        "El servicio de signos vitales "
                                + "es obligatorio."
                );
    }

    // Registra mediciones dentro de una consulta abierta.
    @PostMapping(
            "/consultas/{idConsultas}/signos-vitales"
    )
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<SignosVitalesResponse> registrar(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @PathVariable
            @Positive(
                    message = "El identificador del paciente "
                            + "debe ser mayor que cero."
            )
            final Long idPacientes,

            @PathVariable
            @Positive(
                    message = "El identificador de la consulta "
                            + "debe ser mayor que cero."
            )
            final Long idConsultas,

            @Valid
            @RequestBody
            final SignosVitalesRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final SignosVitalesResponse respuesta =
                signosVitalesService.registrar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idUsuarioResponsable,
                        solicitud
                );

        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{idSignosVitales}")
                        .buildAndExpand(
                                respuesta.idSignosVitales()
                        )
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Lista las mediciones de una consulta médica.
    @GetMapping(
            "/consultas/{idConsultas}/signos-vitales"
    )
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<List<SignosVitalesResponse>>
    listarPorConsulta(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @PathVariable
            @Positive(
                    message = "El identificador del paciente "
                            + "debe ser mayor que cero."
            )
            final Long idPacientes,

            @PathVariable
            @Positive(
                    message = "El identificador de la consulta "
                            + "debe ser mayor que cero."
            )
            final Long idConsultas
    ) {
        final List<SignosVitalesResponse> respuesta =
                signosVitalesService.listarPorConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene un registro específico de signos vitales.
    @GetMapping(
            "/consultas/{idConsultas}"
                    + "/signos-vitales/{idSignosVitales}"
    )
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<SignosVitalesResponse> buscarPorId(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @PathVariable
            @Positive(
                    message = "El identificador del paciente "
                            + "debe ser mayor que cero."
            )
            final Long idPacientes,

            @PathVariable
            @Positive(
                    message = "El identificador de la consulta "
                            + "debe ser mayor que cero."
            )
            final Long idConsultas,

            @PathVariable
            @Positive(
                    message = "El identificador de signos vitales "
                            + "debe ser mayor que cero."
            )
            final Long idSignosVitales
    ) {
        final SignosVitalesResponse respuesta =
                signosVitalesService.buscarPorId(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idSignosVitales
                );

        return ResponseEntity.ok(respuesta);
    }

    // Lista la evolución de mediciones del paciente.
    @GetMapping("/signos-vitales/evolucion")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<List<SignosVitalesResponse>>
    listarEvolucion(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @PathVariable
            @Positive(
                    message = "El identificador del paciente "
                            + "debe ser mayor que cero."
            )
            final Long idPacientes
    ) {
        final List<SignosVitalesResponse> respuesta =
                signosVitalesService.listarEvolucionPaciente(
                        idConsultorios,
                        idPacientes
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene el total de mediciones del paciente.
    @GetMapping("/signos-vitales/conteo")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConteoSignosVitalesResponse> contar(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @PathVariable
            @Positive(
                    message = "El identificador del paciente "
                            + "debe ser mayor que cero."
            )
            final Long idPacientes
    ) {
        final long totalRegistros =
                signosVitalesService.contarRegistrosPaciente(
                        idConsultorios,
                        idPacientes
                );

        final ConteoSignosVitalesResponse respuesta =
                new ConteoSignosVitalesResponse(
                        idConsultorios,
                        idPacientes,
                        totalRegistros
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene idUsuarios desde el sub del JWT.
    private static Long obtenerIdUsuario(
            final JwtAuthenticationToken autenticacion
    ) {
        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion.getToken() == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar "
                            + "al usuario autenticado."
            );
        }

        final String sujeto =
                autenticacion
                        .getToken()
                        .getSubject();

        if (sujeto == null || sujeto.isBlank()) {
            throw new IllegalArgumentException(
                    "El token no contiene "
                            + "un usuario válido."
            );
        }

        try {
            final Long idUsuarios =
                    Long.valueOf(sujeto.trim());

            if (idUsuarios <= 0) {
                throw new IllegalArgumentException(
                        "El identificador del usuario "
                                + "no es válido."
                );
            }

            return idUsuarios;
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El identificador del usuario "
                            + "no tiene un formato válido."
            );
        }
    }

    /**
     * Respuesta resumida para el total de mediciones.
     */
    public record ConteoSignosVitalesResponse(

            Long idConsultorios,

            Long idPacientes,

            long totalRegistros

    ) {
    }
}