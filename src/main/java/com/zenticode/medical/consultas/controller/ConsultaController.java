package com.zenticode.medical.consultas.controller;

import com.zenticode.medical.consultas.dto.ConsultaRequest;
import com.zenticode.medical.consultas.dto.ConsultaResponse;
import com.zenticode.medical.consultas.service.ConsultaService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Expone las operaciones HTTP de consultas médicas.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}"
                + "/pacientes/{idPacientes}/consultas"
)
public class ConsultaController {

    private final ConsultaService consultaService;

    // Inyecta el servicio obligatorio.
    public ConsultaController(
            final ConsultaService consultaService
    ) {
        this.consultaService =
                Objects.requireNonNull(
                        consultaService,
                        "El servicio de consultas es obligatorio."
                );
    }

    // Registra una atención médica en el historial.
    @PostMapping
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConsultaResponse> crear(
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

            @Valid
            @RequestBody
            final ConsultaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final ConsultaResponse respuesta =
                consultaService.crear(
                        idConsultorios,
                        idPacientes,
                        idUsuarioResponsable,
                        solicitud
                );

        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{idConsultas}")
                        .buildAndExpand(
                                respuesta.idConsultas()
                        )
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Lista cronológicamente las consultas del paciente.
    @GetMapping
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<List<ConsultaResponse>> listarHistorial(
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
        final List<ConsultaResponse> respuesta =
                consultaService.listarHistorial(
                        idConsultorios,
                        idPacientes
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene una consulta médica concreta.
    @GetMapping("/{idConsultas}")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConsultaResponse> buscarPorId(
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
        final ConsultaResponse respuesta =
                consultaService.buscarPorId(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        return ResponseEntity.ok(respuesta);
    }

    // Actualiza una consulta que permanece abierta.
    @PutMapping("/{idConsultas}")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConsultaResponse> actualizar(
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
            final ConsultaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final ConsultaResponse respuesta =
                consultaService.actualizar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idUsuarioResponsable,
                        solicitud
                );

        return ResponseEntity.ok(respuesta);
    }

    // Cierra la consulta y protege su contenido.
    @PatchMapping("/{idConsultas}/cerrar")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConsultaResponse> cerrar(
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

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final ConsultaResponse respuesta =
                consultaService.cerrar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idUsuarioResponsable
                );

        return ResponseEntity.ok(respuesta);
    }

    // Anula la consulta conservando la trazabilidad.
    @PatchMapping("/{idConsultas}/anular")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConsultaResponse> anular(
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
            final AnularConsultaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(autenticacion);

        final ConsultaResponse respuesta =
                consultaService.anular(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idUsuarioResponsable,
                        solicitud.motivoAnulacion()
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
     * Cuerpo requerido para anular una consulta.
     */
    public record AnularConsultaRequest(

            @NotBlank(
                    message = "El motivo de anulación "
                            + "es obligatorio."
            )
            @Size(
                    min = 5,
                    max = 500,
                    message = "El motivo de anulación debe contener "
                            + "entre 5 y 500 caracteres."
            )
            String motivoAnulacion

    ) {

        // Normaliza el motivo recibido.
        public AnularConsultaRequest {
            if (motivoAnulacion != null) {
                motivoAnulacion =
                        motivoAnulacion.trim();
            }
        }
    }
}