package com.zenticode.medical.usuarios.service;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import com.zenticode.medical.usuarios.dto.CrearUsuarioRequest;
import com.zenticode.medical.usuarios.dto.UsuarioResponse;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Gestiona las operaciones del módulo de usuarios.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ConsultorioRepository consultorioRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyecta las dependencias obligatorias.
    public UsuarioService(
            final UsuarioRepository usuarioRepository,
            final ConsultorioRepository consultorioRepository,
            final PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = Objects.requireNonNull(
                usuarioRepository,
                "El repositorio de usuarios es obligatorio."
        );

        this.consultorioRepository = Objects.requireNonNull(
                consultorioRepository,
                "El repositorio de consultorios es obligatorio."
        );

        this.passwordEncoder = Objects.requireNonNull(
                passwordEncoder,
                "El codificador de contraseñas es obligatorio."
        );
    }

    // Crea un usuario dentro de un consultorio activo.
    @Transactional
    public UsuarioResponse crear(
            final Long idConsultorios,
            final CrearUsuarioRequest solicitud
    ) {
        validarIdConsultorios(idConsultorios);

        Objects.requireNonNull(
                solicitud,
                "La solicitud de creación del usuario es obligatoria."
        );

        // Busca únicamente consultorios activos.
        final Consultorio consultorio =
                buscarConsultorioActivo(idConsultorios);

        // Comprueba que el correo no esté registrado.
        validarCorreoDisponible(
                idConsultorios,
                solicitud.correo()
        );

        // Convierte la contraseña original en un hash.
        final String passwordHash =
                passwordEncoder.encode(
                        solicitud.password()
                );

        validarPasswordHash(passwordHash);

        // La entidad recibe el hash, nunca la contraseña original.
        final Usuario usuario = new Usuario(
                consultorio,
                solicitud.correo(),
                passwordHash,
                solicitud.nombres(),
                solicitud.apellidos()
        );

        // Añade los datos opcionales.
        usuario.actualizarDatosPersonales(
                solicitud.nombres(),
                solicitud.apellidos(),
                solicitud.numeroColegiatura(),
                solicitud.telefono()
        );

        final Usuario usuarioGuardado =
                usuarioRepository.save(usuario);

        // La respuesta no expone datos de autenticación.
        return UsuarioResponse.desde(usuarioGuardado);
    }

    // Busca un usuario dentro de su consultorio propietario.
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(
            final Long idConsultorios,
            final Long idUsuarios
    ) {
        validarIdConsultorios(idConsultorios);
        validarIdUsuarios(idUsuarios);

        final Usuario usuario =
                usuarioRepository
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

        return UsuarioResponse.desde(usuario);
    }

    // Busca un consultorio activo o devuelve HTTP 404.
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

    // Comprueba que el correo no esté registrado.
    private void validarCorreoDisponible(
            final Long idConsultorios,
            final String correo
    ) {
        final boolean correoRegistrado =
                usuarioRepository
                        .existsByConsultorioIdConsultoriosAndCorreo(
                                idConsultorios,
                                correo
                        );

        if (correoRegistrado) {
            throw new BusinessConflictException(
                    "USER_EMAIL_ALREADY_EXISTS",
                    "El correo ya está registrado en el consultorio."
            );
        }
    }

    // Valida la clave primaria del consultorio.
    private static void validarIdConsultorios(
            final Long idConsultorios
    ) {
        if (idConsultorios == null || idConsultorios <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del consultorio no es válido."
            );
        }
    }

    // Valida la clave primaria del usuario.
    private static void validarIdUsuarios(
            final Long idUsuarios
    ) {
        if (idUsuarios == null || idUsuarios <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no es válido."
            );
        }
    }

    // Comprueba que el codificador haya generado un hash.
    private static void validarPasswordHash(
            final String passwordHash
    ) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalStateException(
                    "No fue posible proteger la contraseña del usuario."
            );
        }
    }
}