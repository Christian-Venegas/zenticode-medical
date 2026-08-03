package com.zenticode.medical.roles.service;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import com.zenticode.medical.roles.entity.Rol;
import com.zenticode.medical.roles.entity.UsuarioRol;
import com.zenticode.medical.roles.repository.RolRepository;
import com.zenticode.medical.roles.repository.UsuarioRolRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Gestiona la asignación de roles a usuarios.
 */
@Service
public class AsignacionRolService {

    private final ConsultorioRepository consultorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    // Inyecta los repositorios obligatorios.
    public AsignacionRolService(
            final ConsultorioRepository consultorioRepository,
            final UsuarioRepository usuarioRepository,
            final RolRepository rolRepository,
            final UsuarioRolRepository usuarioRolRepository
    ) {
        this.consultorioRepository = Objects.requireNonNull(
                consultorioRepository,
                "El repositorio de consultorios es obligatorio."
        );

        this.usuarioRepository = Objects.requireNonNull(
                usuarioRepository,
                "El repositorio de usuarios es obligatorio."
        );

        this.rolRepository = Objects.requireNonNull(
                rolRepository,
                "El repositorio de roles es obligatorio."
        );

        this.usuarioRolRepository = Objects.requireNonNull(
                usuarioRolRepository,
                "El repositorio de asignaciones es obligatorio."
        );
    }

    // Asigna un rol sin indicar un usuario asignador.
    @Transactional
    public void asignar(
            final Long idConsultorios,
            final Long idUsuarios,
            final String codigoRol
    ) {
        asignar(
                idConsultorios,
                idUsuarios,
                codigoRol,
                null
        );
    }

    // Asigna un rol indicando quién realizó la operación.
    @Transactional
    public void asignar(
            final Long idConsultorios,
            final Long idUsuarios,
            final String codigoRol,
            final Long idUsuarioAsignador
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        validarId(
                idUsuarios,
                "El identificador del usuario no es válido."
        );

        final String codigoNormalizado =
                normalizarCodigoRol(codigoRol);

        final Consultorio consultorio =
                buscarConsultorioActivo(idConsultorios);

        final Usuario usuario =
                buscarUsuario(
                        idConsultorios,
                        idUsuarios
                );

        final Rol rol =
                buscarRolActivo(codigoNormalizado);

        validarRolNoAsignado(
                idConsultorios,
                idUsuarios,
                rol.getIdRoles()
        );

        final Usuario asignador =
                buscarAsignadorOpcional(
                        idConsultorios,
                        idUsuarioAsignador
                );

        final UsuarioRol asignacion =
                new UsuarioRol(
                        consultorio,
                        usuario,
                        rol,
                        asignador
                );

        usuarioRolRepository.save(asignacion);
    }

    // Obtiene los códigos de roles activos del usuario.
    @Transactional(readOnly = true)
    public List<String> buscarCodigosActivos(
            final Long idConsultorios,
            final Long idUsuarios
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        validarId(
                idUsuarios,
                "El identificador del usuario no es válido."
        );

        // Evita consultar roles de usuarios de otro consultorio.
        buscarUsuario(
                idConsultorios,
                idUsuarios
        );

        return List.copyOf(
                usuarioRolRepository
                        .buscarCodigosRolesActivos(
                                idConsultorios,
                                idUsuarios
                        )
        );
    }

    // Busca un consultorio activo.
    private Consultorio buscarConsultorioActivo(
            final Long idConsultorios
    ) {
        return consultorioRepository
                .findByIdConsultoriosAndEstado(
                        idConsultorios,
                        EstadoConsultorio.ACTIVO
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "CONSULTORIO_NOT_FOUND",
                                "El consultorio no existe "
                                        + "o no está disponible."
                        )
                );
    }

    // Busca un usuario dentro de su consultorio.
    private Usuario buscarUsuario(
            final Long idConsultorios,
            final Long idUsuarios
    ) {
        return usuarioRepository
                .findByIdUsuariosAndConsultorioIdConsultorios(
                        idUsuarios,
                        idConsultorios
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "USER_NOT_FOUND",
                                "El usuario solicitado no existe."
                        )
                );
    }

    // Busca únicamente roles habilitados.
    private Rol buscarRolActivo(
            final String codigoRol
    ) {
        return rolRepository
                .findByCodigoAndActivoTrue(codigoRol)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "ROLE_NOT_FOUND",
                                "El rol solicitado no existe "
                                        + "o no está disponible."
                        )
                );
    }

    // Busca al usuario que realizó la asignación.
    private Usuario buscarAsignadorOpcional(
            final Long idConsultorios,
            final Long idUsuarioAsignador
    ) {
        if (idUsuarioAsignador == null) {
            return null;
        }

        validarId(
                idUsuarioAsignador,
                "El identificador del usuario asignador "
                        + "no es válido."
        );

        return usuarioRepository
                .findByIdUsuariosAndConsultorioIdConsultorios(
                        idUsuarioAsignador,
                        idConsultorios
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "ASSIGNER_NOT_FOUND",
                                "El usuario asignador no existe."
                        )
                );
    }

    // Evita asignaciones duplicadas.
    private void validarRolNoAsignado(
            final Long idConsultorios,
            final Long idUsuarios,
            final Long idRoles
    ) {
        final boolean asignacionExistente =
                usuarioRolRepository
                        .existsByConsultorioIdConsultoriosAndUsuarioIdUsuariosAndRolIdRoles(
                                idConsultorios,
                                idUsuarios,
                                idRoles
                        );

        if (asignacionExistente) {
            throw new BusinessConflictException(
                    "USER_ROLE_ALREADY_ASSIGNED",
                    "El usuario ya tiene asignado este rol."
            );
        }
    }

    // Normaliza códigos como admin-consultorio.
    private static String normalizarCodigoRol(
            final String codigoRol
    ) {
        if (codigoRol == null || codigoRol.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del rol es obligatorio."
            );
        }

        final String codigoNormalizado =
                codigoRol
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace('-', '_')
                        .replace(' ', '_');

        if (!codigoNormalizado.matches(
                "^[A-Z][A-Z0-9_]*$"
        )) {
            throw new IllegalArgumentException(
                    "El código del rol no tiene un formato válido."
            );
        }

        if (codigoNormalizado.length() > 50) {
            throw new IllegalArgumentException(
                    "El código del rol no puede superar "
                            + "los 50 caracteres."
            );
        }

        return codigoNormalizado;
    }

    // Comprueba que una clave primaria sea positiva.
    private static void validarId(
            final Long identificador,
            final String mensaje
    ) {
        if (identificador == null || identificador <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}