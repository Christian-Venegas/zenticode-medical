package com.zenticode.medical.diagnosticos.controller;

import com.zenticode.medical.diagnosticos.dto.DiagnosticoConsultaRequest;
import com.zenticode.medical.diagnosticos.dto.DiagnosticoConsultaResponse;
import com.zenticode.medical.diagnosticos.service.DiagnosticoConsultaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Expone las operaciones HTTP de diagnósticos clínicos.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}"
                + "/pacientes/{idPacientes}"
)
public class DiagnosticoConsultaController {

    private final DiagnosticoConsultaService
            diagnosticoConsultaService;

    // Inyecta el servicio obligatorio.
    public DiagnosticoConsultaController(
            final DiagnosticoConsultaService
                    diagnosticoConsultaService
    ) {
        this.diagnosticoConsultaService =
                Objects.requireNonNull(
                        diagnosticoConsultaService,
                        "El servicio de diagnósticos "
                                + "es obligatorio."
                );
    }

    // Registra un diagnóstico en una consulta abierta.
    @PostMapping(
            "/consultas/{idConsultas}/diagnosticos"
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
    public ResponseEntity<DiagnosticoConsultaResponse>
    registrar(
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
            final DiagnosticoConsultaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final DiagnosticoConsultaResponse respuesta =
                diagnosticoConsultaService.registrar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idUsuarioResponsable,
                        solicitud
                );

        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{idDiagnosticosConsultas}")
                        .buildAndExpand(
                                respuesta
                                        .idDiagnosticosConsultas()
                        )
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Lista diagnósticos activos o todos para auditoría.
    @GetMapping(
            "/consultas/{idConsultas}/diagnosticos"
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
    public ResponseEntity<List<DiagnosticoConsultaResponse>>
    listar(
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

            @RequestParam(
                    name = "incluirInactivos",
                    defaultValue = "false"
            )
            final boolean incluirInactivos
    ) {
        final List<DiagnosticoConsultaResponse> respuesta =
                incluirInactivos
                        ? diagnosticoConsultaService.listarTodos(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                )
                        : diagnosticoConsultaService.listarActivos(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene el diagnóstico principal activo.
    @GetMapping(
            "/consultas/{idConsultas}"
                    + "/diagnosticos/principal"
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
    public ResponseEntity<DiagnosticoConsultaResponse>
    buscarPrincipal(
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
        final DiagnosticoConsultaResponse respuesta =
                diagnosticoConsultaService.buscarPrincipal(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        return ResponseEntity.ok(respuesta);
    }

    // Devuelve el total de diagnósticos activos.
    @GetMapping(
            "/consultas/{idConsultas}"
                    + "/diagnosticos/conteo"
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
    public ResponseEntity<ConteoDiagnosticosResponse>
    contarActivos(
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
        final long totalDiagnosticos =
                diagnosticoConsultaService.contarActivos(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        final ConteoDiagnosticosResponse respuesta =
                new ConteoDiagnosticosResponse(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        totalDiagnosticos
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene un diagnóstico específico de la consulta.
    @GetMapping(
            "/consultas/{idConsultas}"
                    + "/diagnosticos/{idDiagnosticosConsultas}"
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
    public ResponseEntity<DiagnosticoConsultaResponse>
    buscarPorId(
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
                    message = "El identificador del diagnóstico "
                            + "debe ser mayor que cero."
            )
            final Long idDiagnosticosConsultas
    ) {
        final DiagnosticoConsultaResponse respuesta =
                diagnosticoConsultaService.buscarPorId(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idDiagnosticosConsultas
                );

        return ResponseEntity.ok(respuesta);
    }

    // Actualiza un diagnóstico activo de una consulta abierta.
    @PutMapping(
            "/consultas/{idConsultas}"
                    + "/diagnosticos/{idDiagnosticosConsultas}"
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
    public ResponseEntity<DiagnosticoConsultaResponse>
    actualizar(
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
                    message = "El identificador del diagnóstico "
                            + "debe ser mayor que cero."
            )
            final Long idDiagnosticosConsultas,

            @Valid
            @RequestBody
            final DiagnosticoConsultaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final DiagnosticoConsultaResponse respuesta =
                diagnosticoConsultaService.actualizar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idDiagnosticosConsultas,
                        idUsuarioResponsable,
                        solicitud
                );

        return ResponseEntity.ok(respuesta);
    }

    // Desactiva un diagnóstico conservando su auditoría.
    @PatchMapping(
            "/consultas/{idConsultas}"
                    + "/diagnosticos/{idDiagnosticosConsultas}"
                    + "/desactivar"
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
    public ResponseEntity<DiagnosticoConsultaResponse>
    desactivar(
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
                    message = "El identificador del diagnóstico "
                            + "debe ser mayor que cero."
            )
            final Long idDiagnosticosConsultas,

            @Valid
            @RequestBody
            final DesactivarDiagnosticoRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final DiagnosticoConsultaResponse respuesta =
                diagnosticoConsultaService.desactivar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idDiagnosticosConsultas,
                        idUsuarioResponsable,
                        solicitud.motivoDesactivacion()
                );

        return ResponseEntity.ok(respuesta);
    }

    // Lista la evolución diagnóstica activa del paciente.
    @GetMapping("/diagnosticos/evolucion")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<List<DiagnosticoConsultaResponse>>
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
        final List<DiagnosticoConsultaResponse> respuesta =
                diagnosticoConsultaService
                        .listarEvolucionPaciente(
                                idConsultorios,
                                idPacientes
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
                            + "al profesional autenticado."
            );
        }

        final String sujeto =
                autenticacion
                        .getToken()
                        .getSubject();

        if (sujeto == null || sujeto.isBlank()) {
            throw new IllegalArgumentException(
                    "El token no contiene "
                            + "un profesional válido."
            );
        }

        try {
            final Long idUsuarios =
                    Long.valueOf(sujeto.trim());

            if (idUsuarios <= 0) {
                throw new IllegalArgumentException(
                        "El identificador del profesional "
                                + "no es válido."
                );
            }

            return idUsuarios;
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    "El identificador del profesional "
                            + "no tiene un formato válido."
            );
        }
    }

    /**
     * Respuesta resumida del total de diagnósticos activos.
     */
    public record ConteoDiagnosticosResponse(

            Long idConsultorios,

            Long idPacientes,

            Long idConsultas,

            long totalDiagnosticosActivos

    ) {
    }

    /**
     * Motivo obligatorio para desactivar un diagnóstico.
     */
    public record DesactivarDiagnosticoRequest(

            @NotBlank(
                    message = "El motivo de desactivación "
                            + "es obligatorio."
            )
            @Size(
                    min = 5,
                    max = 500,
                    message = "El motivo de desactivación debe contener "
                            + "entre 5 y 500 caracteres."
            )
            String motivoDesactivacion

    ) {

        // Normaliza la justificación recibida.
        public DesactivarDiagnosticoRequest {
            if (motivoDesactivacion != null) {
                motivoDesactivacion =
                        motivoDesactivacion.trim();
            }
        }
    }
}