package com.zenticode.medical.usuarios.controller;

import com.zenticode.medical.usuarios.dto.CrearUsuarioRequest;
import com.zenticode.medical.usuarios.dto.UsuarioResponse;
import com.zenticode.medical.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

/**
 * Expone las operaciones HTTP del módulo de usuarios.
 */
@Validated
@RestController
@RequestMapping(
        "/api/v1/consultorios/{idConsultorios}/usuarios"
)
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Inyecta el servicio obligatorio.
    public UsuarioController(
            final UsuarioService usuarioService
    ) {
        this.usuarioService = Objects.requireNonNull(
                usuarioService,
                "El servicio de usuarios es obligatorio."
        );
    }

    // Crea un usuario dentro del consultorio autenticado.
    @PostMapping
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO'"
                    + ")"
    )
    public ResponseEntity<UsuarioResponse> crear(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @Valid
            @RequestBody
            final CrearUsuarioRequest solicitud
    ) {
        final UsuarioResponse respuesta =
                usuarioService.crear(
                        idConsultorios,
                        solicitud
                );

        // Construye la ubicación pública del nuevo usuario.
        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{idUsuarios}")
                        .buildAndExpand(
                                respuesta.idUsuarios()
                        )
                        .toUri();

        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }

    // Busca un usuario dentro del consultorio autenticado.
    @GetMapping("/{idUsuarios}")
    @PreAuthorize(
            "@tenantSecurityService."
                    + "perteneceAlConsultorioConRol("
                    + "authentication, "
                    + "#idConsultorios, "
                    + "'ADMIN_CONSULTORIO'"
                    + ")"
    )
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable
            @Positive(
                    message = "El identificador del consultorio "
                            + "debe ser mayor que cero."
            )
            final Long idConsultorios,

            @PathVariable
            @Positive(
                    message = "El identificador del usuario "
                            + "debe ser mayor que cero."
            )
            final Long idUsuarios
    ) {
        final UsuarioResponse respuesta =
                usuarioService.buscarPorId(
                        idConsultorios,
                        idUsuarios
                );

        return ResponseEntity.ok(respuesta);
    }
}