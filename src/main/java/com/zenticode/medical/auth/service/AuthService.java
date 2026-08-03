package com.zenticode.medical.auth.service;

import com.zenticode.medical.auth.dto.LoginRequest;
import com.zenticode.medical.auth.dto.LoginResponse;
import com.zenticode.medical.auth.service.JwtService.TokenGenerado;
import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import com.zenticode.medical.roles.repository.UsuarioRolRepository;
import com.zenticode.medical.shared.exception.InvalidCredentialsException;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.entity.Usuario.EstadoUsuario;
import com.zenticode.medical.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Gestiona la autenticación de usuarios.
 */
@Service
public class AuthService {

    private final ConsultorioRepository consultorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Inyecta las dependencias obligatorias.
    public AuthService(
            final ConsultorioRepository consultorioRepository,
            final UsuarioRepository usuarioRepository,
            final UsuarioRolRepository usuarioRolRepository,
            final PasswordEncoder passwordEncoder,
            final JwtService jwtService
    ) {
        this.consultorioRepository = Objects.requireNonNull(
                consultorioRepository,
                "El repositorio de consultorios es obligatorio."
        );

        this.usuarioRepository = Objects.requireNonNull(
                usuarioRepository,
                "El repositorio de usuarios es obligatorio."
        );

        this.usuarioRolRepository = Objects.requireNonNull(
                usuarioRolRepository,
                "El repositorio de roles de usuario es obligatorio."
        );

        this.passwordEncoder = Objects.requireNonNull(
                passwordEncoder,
                "El codificador de contraseñas es obligatorio."
        );

        this.jwtService = Objects.requireNonNull(
                jwtService,
                "El servicio JWT es obligatorio."
        );
    }

    // Valida las credenciales y genera un token de acceso.
    @Transactional
    public LoginResponse iniciarSesion(
            final LoginRequest solicitud
    ) {
        Objects.requireNonNull(
                solicitud,
                "La solicitud de inicio de sesión es obligatoria."
        );

        final Consultorio consultorio =
                buscarConsultorioActivo(
                        solicitud.idConsultorios()
                );

        final Usuario usuario =
                buscarUsuarioActivo(
                        consultorio.getIdConsultorios(),
                        solicitud.correo()
                );

        final OffsetDateTime momentoAcceso =
                OffsetDateTime.now();

        // Rechaza cuentas con bloqueo temporal vigente.
        validarCuentaNoBloqueada(
                usuario,
                momentoAcceso
        );

        // Compara la contraseña original con BCrypt.
        validarPassword(
                solicitud.password(),
                usuario.getPasswordHash()
        );

        final List<String> roles =
                buscarRolesActivos(
                        consultorio.getIdConsultorios(),
                        usuario.getIdUsuarios()
                );

        // Genera el token únicamente tras validar el acceso.
        final TokenGenerado tokenGenerado =
                jwtService.generarToken(
                        usuario.getIdUsuarios(),
                        consultorio.getIdConsultorios(),
                        roles
                );

        // Limpia intentos fallidos y registra el acceso.
        usuario.registrarAccesoCorrecto(momentoAcceso);

        return construirRespuesta(
                usuario,
                consultorio,
                roles,
                momentoAcceso,
                tokenGenerado
        );
    }

    // Busca únicamente un consultorio activo.
    private Consultorio buscarConsultorioActivo(
            final Long idConsultorios
    ) {
        if (idConsultorios == null || idConsultorios <= 0) {
            throw new InvalidCredentialsException();
        }

        return consultorioRepository
                .findByIdConsultoriosAndEstado(
                        idConsultorios,
                        EstadoConsultorio.ACTIVO
                )
                .orElseThrow(
                        InvalidCredentialsException::new
                );
    }

    // Busca una cuenta activa dentro del consultorio.
    private Usuario buscarUsuarioActivo(
            final Long idConsultorios,
            final String correo
    ) {
        if (correo == null || correo.isBlank()) {
            throw new InvalidCredentialsException();
        }

        return usuarioRepository
                .findByConsultorioIdConsultoriosAndCorreoAndEstado(
                        idConsultorios,
                        correo,
                        EstadoUsuario.ACTIVO
                )
                .orElseThrow(
                        InvalidCredentialsException::new
                );
    }

    // Rechaza usuarios con bloqueo temporal vigente.
    private static void validarCuentaNoBloqueada(
            final Usuario usuario,
            final OffsetDateTime momentoAcceso
    ) {
        if (usuario.estaBloqueadoTemporalmente(momentoAcceso)) {
            throw new InvalidCredentialsException();
        }
    }

    // Comprueba la contraseña sin exponer el motivo del fallo.
    private void validarPassword(
            final String password,
            final String passwordHash
    ) {
        if (password == null
                || password.isBlank()
                || passwordHash == null
                || passwordHash.isBlank()) {
            throw new InvalidCredentialsException();
        }

        final boolean passwordCorrecto;

        try {
            passwordCorrecto =
                    passwordEncoder.matches(
                            password,
                            passwordHash
                    );
        } catch (IllegalArgumentException excepcion) {
            // Oculta un posible hash almacenado con formato inválido.
            throw new InvalidCredentialsException();
        }

        if (!passwordCorrecto) {
            throw new InvalidCredentialsException();
        }
    }

    // Obtiene únicamente los roles activos del usuario.
    private List<String> buscarRolesActivos(
            final Long idConsultorios,
            final Long idUsuarios
    ) {
        final List<String> roles =
                usuarioRolRepository
                        .buscarCodigosRolesActivos(
                                idConsultorios,
                                idUsuarios
                        );

        if (roles == null || roles.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        return List.copyOf(roles);
    }

    // Construye la respuesta segura con el JWT.
    private static LoginResponse construirRespuesta(
            final Usuario usuario,
            final Consultorio consultorio,
            final List<String> roles,
            final OffsetDateTime momentoAcceso,
            final TokenGenerado tokenGenerado
    ) {
        return new LoginResponse(
                usuario.getIdUsuarios(),
                consultorio.getIdConsultorios(),
                usuario.getCorreo(),
                usuario.getNombres(),
                usuario.getApellidos(),
                roles,
                momentoAcceso,
                tokenGenerado.token(),
                tokenGenerado.tipo(),
                tokenGenerado.expiraEn()
        );
    }
}