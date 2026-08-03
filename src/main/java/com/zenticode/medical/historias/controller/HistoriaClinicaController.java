package com.zenticode.medical.historias.controller;

import com.zenticode.medical.historias.dto.HistoriaClinicaRequest;
import com.zenticode.medical.historias.dto.HistoriaClinicaResponse;
import com.zenticode.medical.historias.service.HistoriaClinicaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import java.util.Objects;

/**
 * Expone las operaciones HTTP de historias clínicas.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}"
                + "/pacientes/{idPacientes}/historia-clinica"
)
public class HistoriaClinicaController {

    private static final String MENSAJE_CONSULTORIO =
            "El identificador del consultorio "
                    + "debe ser mayor que cero.";

    private static final String MENSAJE_PACIENTE =
            "El identificador del paciente "
                    + "debe ser mayor que cero.";

    private final HistoriaClinicaService
            historiaClinicaService;

    // Inyecta el servicio obligatorio.
    public HistoriaClinicaController(
            final HistoriaClinicaService
                    historiaClinicaService
    ) {
        this.historiaClinicaService =
                Objects.requireNonNull(
                        historiaClinicaService,
                        "El servicio de historias clínicas "
                                + "es obligatorio."
                );
    }

    // Abre la historia clínica general.
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
    public ResponseEntity<HistoriaClinicaResponse> abrir(
            @PathVariable
            @Positive(message = MENSAJE_CONSULTORIO)
            final Long idConsultorios,

            @PathVariable
            @Positive(message = MENSAJE_PACIENTE)
            final Long idPacientes,

            @Valid
            @RequestBody
            final HistoriaClinicaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final HistoriaClinicaResponse respuesta =
                historiaClinicaService.abrir(
                        idConsultorios,
                        idPacientes,
                        idUsuarioResponsable,
                        solicitud
                );

        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .build()
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Obtiene la historia clínica activa.
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
    public ResponseEntity<HistoriaClinicaResponse>
    buscarActiva(
            @PathVariable
            @Positive(message = MENSAJE_CONSULTORIO)
            final Long idConsultorios,

            @PathVariable
            @Positive(message = MENSAJE_PACIENTE)
            final Long idPacientes
    ) {
        final HistoriaClinicaResponse respuesta =
                historiaClinicaService.buscarActiva(
                        idConsultorios,
                        idPacientes
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Actualiza el resumen clínico general.
    @PutMapping
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<HistoriaClinicaResponse>
    actualizar(
            @PathVariable
            @Positive(message = MENSAJE_CONSULTORIO)
            final Long idConsultorios,

            @PathVariable
            @Positive(message = MENSAJE_PACIENTE)
            final Long idPacientes,

            @Valid
            @RequestBody
            final HistoriaClinicaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final HistoriaClinicaResponse respuesta =
                historiaClinicaService.actualizar(
                        idConsultorios,
                        idPacientes,
                        idUsuarioResponsable,
                        solicitud
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Archiva la historia sin eliminar datos.
    @PatchMapping("/archivar")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<HistoriaClinicaResponse>
    archivar(
            @PathVariable
            @Positive(message = MENSAJE_CONSULTORIO)
            final Long idConsultorios,

            @PathVariable
            @Positive(message = MENSAJE_PACIENTE)
            final Long idPacientes,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final HistoriaClinicaResponse respuesta =
                historiaClinicaService.archivar(
                        idConsultorios,
                        idPacientes,
                        idUsuarioResponsable
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Cierra la historia usando el nuevo contrato.
    @PatchMapping("/cerrar")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<HistoriaClinicaResponse>
    cerrar(
            @PathVariable
            @Positive(message = MENSAJE_CONSULTORIO)
            final Long idConsultorios,

            @PathVariable
            @Positive(message = MENSAJE_PACIENTE)
            final Long idPacientes,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final HistoriaClinicaResponse respuesta =
                historiaClinicaService.cerrar(
                        idConsultorios,
                        idPacientes,
                        idUsuarioResponsable
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Reactiva una historia clínica cerrada.
    @PatchMapping("/reabrir")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<HistoriaClinicaResponse>
    reabrir(
            @PathVariable
            @Positive(message = MENSAJE_CONSULTORIO)
            final Long idConsultorios,

            @PathVariable
            @Positive(message = MENSAJE_PACIENTE)
            final Long idPacientes,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final HistoriaClinicaResponse respuesta =
                historiaClinicaService.reabrir(
                        idConsultorios,
                        idPacientes,
                        idUsuarioResponsable
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Obtiene idUsuarios desde el claim sub.
    private static Long obtenerIdUsuario(
            final JwtAuthenticationToken autenticacion
    ) {
        if (
                autenticacion == null
                        || !autenticacion.isAuthenticated()
                        || autenticacion.getToken() == null
        ) {
            throw new IllegalArgumentException(
                    "No se pudo identificar "
                            + "al profesional autenticado."
            );
        }

        final String sujeto =
                autenticacion
                        .getToken()
                        .getSubject();

        if (
                sujeto == null
                        || sujeto.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El token no contiene "
                            + "un profesional válido."
            );
        }

        final Long idUsuarios =
                convertirIdUsuario(
                        sujeto
                );

        if (idUsuarios <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del profesional "
                            + "no es válido."
            );
        }

        return idUsuarios;
    }

    // Convierte el subject del JWT en idUsuarios.
    private static Long convertirIdUsuario(
            final String sujeto
    ) {
        try {
            return Long.valueOf(
                    sujeto.trim()
            );
        } catch (
                NumberFormatException excepcion
        ) {
            throw new IllegalArgumentException(
                    "El identificador del profesional "
                            + "no tiene un formato válido.",
                    excepcion
            );
        }
    }
}