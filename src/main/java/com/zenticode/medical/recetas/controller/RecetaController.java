package com.zenticode.medical.recetas.controller;

import com.zenticode.medical.recetas.dto.RecetaRequest;
import com.zenticode.medical.recetas.dto.RecetaResponse;
import com.zenticode.medical.recetas.service.RecetaPdfService;
import com.zenticode.medical.recetas.service.RecetaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Expone las operaciones HTTP de recetas médicas.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}"
                + "/pacientes/{idPacientes}"
)
public class RecetaController {

    private final RecetaService recetaService;

    private final RecetaPdfService recetaPdfService;

    // Inyecta los servicios obligatorios.
    public RecetaController(
            final RecetaService recetaService,
            final RecetaPdfService recetaPdfService
    ) {
        this.recetaService =
                Objects.requireNonNull(
                        recetaService,
                        "El servicio de recetas es obligatorio."
                );

        this.recetaPdfService =
                Objects.requireNonNull(
                        recetaPdfService,
                        "El servicio PDF de recetas es obligatorio."
                );
    }

    // Emite una receta completa para una consulta.
    @PostMapping(
            "/consultas/{idConsultas}/recetas"
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
    public ResponseEntity<RecetaResponse> emitir(
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
            final RecetaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final RecetaResponse respuesta =
                recetaService.emitir(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idUsuarioResponsable,
                        solicitud
                );

        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{idRecetas}")
                        .buildAndExpand(
                                respuesta.idRecetas()
                        )
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Lista las recetas pertenecientes a una consulta.
    @GetMapping(
            "/consultas/{idConsultas}/recetas"
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
    public ResponseEntity<List<RecetaResponse>>
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
            final Long idConsultas,

            @RequestParam(
                    name = "incluirAnuladas",
                    defaultValue = "false"
            )
            final boolean incluirAnuladas
    ) {
        final List<RecetaResponse> respuesta =
                recetaService.listarPorConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        incluirAnuladas
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Obtiene una receta específica con sus medicamentos.
    @GetMapping(
            "/consultas/{idConsultas}"
                    + "/recetas/{idRecetas}"
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
    public ResponseEntity<RecetaResponse> buscarPorId(
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
                    message = "El identificador de la receta "
                            + "debe ser mayor que cero."
            )
            final Long idRecetas
    ) {
        final RecetaResponse respuesta =
                recetaService.buscarPorId(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idRecetas
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Genera y descarga el PDF de una receta médica.
    @GetMapping(
            value = "/consultas/{idConsultas}"
                    + "/recetas/{idRecetas}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
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
    public ResponseEntity<byte[]> descargarPdf(
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
                    message = "El identificador de la receta "
                            + "debe ser mayor que cero."
            )
            final Long idRecetas
    ) {
        final byte[] documento =
                recetaPdfService.generar(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idRecetas
                );

        if (documento.length == 0) {
            throw new IllegalStateException(
                    "El documento PDF de la receta está vacío."
            );
        }

        final String nombreArchivo =
                String.format(
                        "receta-%06d.pdf",
                        idRecetas
                );

        final ContentDisposition disposicion =
                ContentDisposition
                        .attachment()
                        .filename(
                                nombreArchivo,
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposicion.toString()
                )
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, must-revalidate"
                )
                .header(
                        HttpHeaders.PRAGMA,
                        "no-cache"
                )
                .header(
                        HttpHeaders.EXPIRES,
                        "0"
                )
                .contentLength(
                        documento.length
                )
                .body(documento);
    }

    // Anula una receta sin eliminar su contenido.
    @PatchMapping(
            "/consultas/{idConsultas}"
                    + "/recetas/{idRecetas}/anular"
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
    public ResponseEntity<RecetaResponse> anular(
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
                    message = "El identificador de la receta "
                            + "debe ser mayor que cero."
            )
            final Long idRecetas,

            @Valid
            @RequestBody
            final AnularRecetaRequest solicitud,

            final JwtAuthenticationToken autenticacion
    ) {
        final Long idUsuarioResponsable =
                obtenerIdUsuario(
                        autenticacion
                );

        final RecetaResponse respuesta =
                recetaService.anular(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idRecetas,
                        idUsuarioResponsable,
                        solicitud.motivoAnulacion()
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Lista el historial de recetas del paciente.
    @GetMapping("/recetas/historial")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<List<RecetaResponse>>
    listarHistorialPaciente(
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

            @RequestParam(
                    name = "incluirAnuladas",
                    defaultValue = "false"
            )
            final boolean incluirAnuladas
    ) {
        final List<RecetaResponse> respuesta =
                recetaService.listarHistorialPaciente(
                        idConsultorios,
                        idPacientes,
                        incluirAnuladas
                );

        return ResponseEntity.ok(
                respuesta
        );
    }

    // Devuelve el total de recetas emitidas del paciente.
    @GetMapping("/recetas/conteo")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO', "
                    + "'MEDICO'"
                    + ")"
    )
    public ResponseEntity<ConteoRecetasResponse>
    contarEmitidas(
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
        final long totalRecetasEmitidas =
                recetaService.contarEmitidasPaciente(
                        idConsultorios,
                        idPacientes
                );

        final ConteoRecetasResponse respuesta =
                new ConteoRecetasResponse(
                        idConsultorios,
                        idPacientes,
                        totalRecetasEmitidas
                );

        return ResponseEntity.ok(
                respuesta
        );
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
                    Long.valueOf(
                            sujeto.trim()
                    );

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
                            + "no tiene un formato válido.",
                    excepcion
            );
        }
    }

    /**
     * Justificación obligatoria para anular una receta.
     */
    public record AnularRecetaRequest(

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

        // Normaliza la justificación recibida.
        public AnularRecetaRequest {
            if (motivoAnulacion != null) {
                motivoAnulacion =
                        motivoAnulacion.trim();
            }
        }
    }

    /**
     * Respuesta resumida del total de recetas emitidas.
     */
    public record ConteoRecetasResponse(

            Long idConsultorios,

            Long idPacientes,

            long totalRecetasEmitidas

    ) {
    }
}