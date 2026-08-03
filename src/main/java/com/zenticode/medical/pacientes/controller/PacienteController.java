package com.zenticode.medical.pacientes.controller;

import com.zenticode.medical.pacientes.dto.PacienteRequest;
import com.zenticode.medical.pacientes.dto.PacienteResponse;
import com.zenticode.medical.pacientes.service.PacienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Expone las operaciones HTTP del módulo de pacientes.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}/pacientes"
)
public class PacienteController {

    private final PacienteService pacienteService;

    // Inyecta el servicio obligatorio.
    public PacienteController(
            final PacienteService pacienteService
    ) {
        this.pacienteService = Objects.requireNonNull(
                pacienteService,
                "El servicio de pacientes es obligatorio."
        );
    }

    // Registra un paciente en el consultorio autenticado.
    @PostMapping
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO', "
                    + "'ASISTENTE'"
                    + ")"
    )
    public ResponseEntity<PacienteResponse> crear(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @Valid
            @RequestBody
            final PacienteRequest solicitud
    ) {
        final PacienteResponse respuesta =
                pacienteService.crear(
                        idConsultorios,
                        solicitud
                );

        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{idPacientes}")
                        .buildAndExpand(
                                respuesta.idPacientes()
                        )
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Lista pacientes o aplica una búsqueda opcional.
    @GetMapping
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO', "
                    + "'ASISTENTE'"
                    + ")"
    )
    public ResponseEntity<List<PacienteResponse>> listar(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @RequestParam(
                    name = "buscar",
                    required = false
            )
            @Size(
                    max = 100,
                    message = "El término de búsqueda no puede "
                            + "superar los 100 caracteres."
            )
            final String buscar
    ) {
        final List<PacienteResponse> respuesta =
                buscar == null || buscar.isBlank()
                        ? pacienteService.listarActivos(
                        idConsultorios
                )
                        : pacienteService.buscar(
                        idConsultorios,
                        buscar
                );

        return ResponseEntity.ok(respuesta);
    }

    // Obtiene un paciente dentro del consultorio autenticado.
    @GetMapping("/{idPacientes}")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO', "
                    + "'ASISTENTE'"
                    + ")"
    )
    public ResponseEntity<PacienteResponse> buscarPorId(
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
        final PacienteResponse respuesta =
                pacienteService.buscarPorId(
                        idConsultorios,
                        idPacientes
                );

        return ResponseEntity.ok(respuesta);
    }

    // Actualiza los datos administrativos del paciente.
    @PutMapping("/{idPacientes}")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO', "
                    + "'ASISTENTE'"
                    + ")"
    )
    public ResponseEntity<PacienteResponse> actualizar(
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
            final PacienteRequest solicitud
    ) {
        final PacienteResponse respuesta =
                pacienteService.actualizar(
                        idConsultorios,
                        idPacientes,
                        solicitud
                );

        return ResponseEntity.ok(respuesta);
    }

    // Desactiva al paciente sin eliminar su información.
    @PatchMapping("/{idPacientes}/desactivar")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<PacienteResponse> desactivar(
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
        final PacienteResponse respuesta =
                pacienteService.desactivar(
                        idConsultorios,
                        idPacientes
                );

        return ResponseEntity.ok(respuesta);
    }
}