package com.zenticode.medical.usuarios.service;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import com.zenticode.medical.usuarios.dto.CrearUsuarioRequest;
import com.zenticode.medical.usuarios.dto.UsuarioResponse;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zenticode.medical.shared.exception.BusinessConflictException;

import java.util.Optional;

import com.zenticode.medical.shared.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de UsuarioService.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Long ID_CONSULTORIOS = 1L;
    private static final Long ID_USUARIOS = 10L;

    private static final String PASSWORD_ORIGINAL =
            "FraseSegura2026";

    private static final String PASSWORD_HASH =
            "$2a$12$hashSimuladoParaPruebas";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ConsultorioRepository consultorioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    // Crea un servicio nuevo antes de cada prueba.
    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                consultorioRepository,
                passwordEncoder
        );
    }

    @Test
    @DisplayName(
            "Debe crear un usuario con contraseña protegida"
    )
    void debeCrearUsuarioConPasswordProtegido() {
        final Consultorio consultorio =
                new Consultorio(
                        "consultorio-demo",
                        "Consultorio Demo"
                );

        final CrearUsuarioRequest solicitud =
                crearSolicitudValida();

        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.of(consultorio));

        when(
                usuarioRepository
                        .existsByConsultorioIdConsultoriosAndCorreo(
                                ID_CONSULTORIOS,
                                "medico@consultorio.test"
                        )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(PASSWORD_ORIGINAL)
        ).thenReturn(PASSWORD_HASH);

        when(
                usuarioRepository.save(any(Usuario.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));

        final UsuarioResponse respuesta =
                usuarioService.crear(
                        ID_CONSULTORIOS,
                        solicitud
                );

        assertNotNull(respuesta);
        assertEquals(
                "medico@consultorio.test",
                respuesta.correo()
        );
        assertEquals(
                "Nombre Médico",
                respuesta.nombres()
        );
        assertEquals(
                "Apellido Demo",
                respuesta.apellidos()
        );
        assertEquals(
                "CMP-12345",
                respuesta.numeroColegiatura()
        );
        assertEquals(
                "999999999",
                respuesta.telefono()
        );
        assertEquals(
                Usuario.EstadoUsuario.ACTIVO,
                respuesta.estado()
        );

        // PostgreSQL no participa en esta prueba unitaria.
        assertNull(respuesta.idUsuarios());
        assertNull(respuesta.idConsultorios());
        assertNull(respuesta.fechaCreacion());
        assertNull(respuesta.fechaModificacion());

        final ArgumentCaptor<Usuario> capturador =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(
                capturador.capture()
        );

        final Usuario usuarioGuardado =
                capturador.getValue();

        assertEquals(
                PASSWORD_HASH,
                usuarioGuardado.getPasswordHash()
        );

        // Confirma que nunca llega la contraseña original a la entidad.
        assertNotEquals(
                PASSWORD_ORIGINAL,
                usuarioGuardado.getPasswordHash()
        );

        assertEquals(
                "medico@consultorio.test",
                usuarioGuardado.getCorreo()
        );

        assertEquals(
                consultorio,
                usuarioGuardado.getConsultorio()
        );

        verify(
                passwordEncoder,
                times(1)
        ).encode(PASSWORD_ORIGINAL);

        verify(
                usuarioRepository,
                times(1)
        ).save(any(Usuario.class));
    }

    @Test
    @DisplayName(
            "Debe normalizar el correo antes de comprobar duplicados"
    )
    void debeNormalizarCorreoAntesDeCrear() {
        final Consultorio consultorio =
                new Consultorio(
                        "consultorio-demo",
                        "Consultorio Demo"
                );

        final CrearUsuarioRequest solicitud =
                new CrearUsuarioRequest(
                        "  MEDICO@CONSULTORIO.TEST  ",
                        PASSWORD_ORIGINAL,
                        "Nombre Médico",
                        "Apellido Demo",
                        null,
                        null
                );

        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.of(consultorio));

        when(
                usuarioRepository
                        .existsByConsultorioIdConsultoriosAndCorreo(
                                ID_CONSULTORIOS,
                                "medico@consultorio.test"
                        )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(PASSWORD_ORIGINAL)
        ).thenReturn(PASSWORD_HASH);

        when(
                usuarioRepository.save(any(Usuario.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));

        final UsuarioResponse respuesta =
                usuarioService.crear(
                        ID_CONSULTORIOS,
                        solicitud
                );

        assertEquals(
                "medico@consultorio.test",
                respuesta.correo()
        );

        verify(
                usuarioRepository
        ).existsByConsultorioIdConsultoriosAndCorreo(
                ID_CONSULTORIOS,
                "medico@consultorio.test"
        );
    }

    @Test
    @DisplayName(
            "Debe rechazar un correo duplicado con conflicto de negocio"
    )
    void debeRechazarCorreoDuplicado() {
        final Consultorio consultorio =
                new Consultorio(
                        "consultorio-demo",
                        "Consultorio Demo"
                );

        final CrearUsuarioRequest solicitud =
                crearSolicitudValida();

        // Simula un consultorio activo.
        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.of(consultorio));

        // Simula que el correo ya está registrado.
        when(
                usuarioRepository
                        .existsByConsultorioIdConsultoriosAndCorreo(
                                ID_CONSULTORIOS,
                                solicitud.correo()
                        )
        ).thenReturn(true);

        final BusinessConflictException excepcion =
                assertThrows(
                        BusinessConflictException.class,
                        () -> usuarioService.crear(
                                ID_CONSULTORIOS,
                                solicitud
                        )
                );

        // Comprueba el código estable para el frontend.
        assertEquals(
                "USER_EMAIL_ALREADY_EXISTS",
                excepcion.getCodigo()
        );

        assertEquals(
                "El correo ya está registrado en el consultorio.",
                excepcion.getMessage()
        );

        // No debe codificar la contraseña si el correo está ocupado.
        verify(
                passwordEncoder,
                never()
        ).encode(any());

        // No debe guardar ningún usuario.
        verify(
                usuarioRepository,
                never()
        ).save(any(Usuario.class));
    }

    @Test
    @DisplayName(
            "Debe rechazar un consultorio inexistente o no disponible"
    )
    void debeRechazarConsultorioNoDisponible() {
        final CrearUsuarioRequest solicitud =
                crearSolicitudValida();

        // Simula que no existe un consultorio activo.
        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.empty());

        final ResourceNotFoundException excepcion =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.crear(
                                ID_CONSULTORIOS,
                                solicitud
                        )
                );

        // Comprueba el código que utilizará el frontend.
        assertEquals(
                "CONSULTORIO_NOT_FOUND",
                excepcion.getCodigo()
        );

        assertEquals(
                "El consultorio no existe o no está disponible.",
                excepcion.getMessage()
        );

        // No debe comprobar correos si el consultorio no existe.
        verify(
                usuarioRepository,
                never()
        ).existsByConsultorioIdConsultoriosAndCorreo(
                any(),
                any()
        );

        // No debe proteger la contraseña si el consultorio no existe.
        verify(
                passwordEncoder,
                never()
        ).encode(any());

        // No debe guardar ningún usuario.
        verify(
                usuarioRepository,
                never()
        ).save(any(Usuario.class));
    }

    @Test
    @DisplayName(
            "Debe rechazar una solicitud nula"
    )
    void debeRechazarSolicitudNula() {
        final NullPointerException excepcion =
                assertThrows(
                        NullPointerException.class,
                        () -> usuarioService.crear(
                                ID_CONSULTORIOS,
                                null
                        )
                );

        assertEquals(
                "La solicitud de creación del usuario es obligatoria.",
                excepcion.getMessage()
        );

        verify(
                consultorioRepository,
                never()
        ).findByIdConsultoriosAndEstado(
                any(),
                any()
        );

        verify(
                usuarioRepository,
                never()
        ).save(any(Usuario.class));
    }

    @Test
    @DisplayName(
            "Debe rechazar un identificador de consultorio inválido"
    )
    void debeRechazarIdConsultorioInvalido() {
        final CrearUsuarioRequest solicitud =
                crearSolicitudValida();

        final IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.crear(
                                0L,
                                solicitud
                        )
                );

        assertEquals(
                "El identificador del consultorio no es válido.",
                excepcion.getMessage()
        );

        verify(
                consultorioRepository,
                never()
        ).findByIdConsultoriosAndEstado(
                any(),
                any()
        );

        verify(
                usuarioRepository,
                never()
        ).save(any(Usuario.class));
    }

    @Test
    @DisplayName(
            "Debe rechazar un hash vacío producido por el codificador"
    )
    void debeRechazarPasswordHashVacio() {
        final Consultorio consultorio =
                new Consultorio(
                        "consultorio-demo",
                        "Consultorio Demo"
                );

        final CrearUsuarioRequest solicitud =
                crearSolicitudValida();

        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.of(consultorio));

        when(
                usuarioRepository
                        .existsByConsultorioIdConsultoriosAndCorreo(
                                ID_CONSULTORIOS,
                                solicitud.correo()
                        )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(PASSWORD_ORIGINAL)
        ).thenReturn(" ");

        final IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> usuarioService.crear(
                                ID_CONSULTORIOS,
                                solicitud
                        )
                );

        assertEquals(
                "No fue posible proteger la contraseña del usuario.",
                excepcion.getMessage()
        );

        verify(
                usuarioRepository,
                never()
        ).save(any(Usuario.class));
    }

    @Test
    @DisplayName(
            "Debe buscar un usuario dentro de su consultorio"
    )
    void debeBuscarUsuarioDentroDelConsultorio() {
        final Consultorio consultorio =
                new Consultorio(
                        "consultorio-demo",
                        "Consultorio Demo"
                );

        final Usuario usuario =
                new Usuario(
                        consultorio,
                        "medico@consultorio.test",
                        PASSWORD_HASH,
                        "Nombre Médico",
                        "Apellido Demo"
                );

        when(
                usuarioRepository
                        .findByIdUsuariosAndConsultorioIdConsultorios(
                                ID_USUARIOS,
                                ID_CONSULTORIOS
                        )
        ).thenReturn(Optional.of(usuario));

        final UsuarioResponse respuesta =
                usuarioService.buscarPorId(
                        ID_CONSULTORIOS,
                        ID_USUARIOS
                );

        assertNotNull(respuesta);
        assertEquals(
                "medico@consultorio.test",
                respuesta.correo()
        );
        assertEquals(
                "Nombre Médico",
                respuesta.nombres()
        );

        verify(
                usuarioRepository,
                times(1)
        ).findByIdUsuariosAndConsultorioIdConsultorios(
                ID_USUARIOS,
                ID_CONSULTORIOS
        );
    }

    @Test
    @DisplayName(
            "Debe ocultar usuarios inexistentes o de otro consultorio"
    )
    void debeRechazarUsuarioFueraDelConsultorio() {
        // Simula que el usuario no pertenece al consultorio.
        when(
                usuarioRepository
                        .findByIdUsuariosAndConsultorioIdConsultorios(
                                ID_USUARIOS,
                                ID_CONSULTORIOS
                        )
        ).thenReturn(Optional.empty());

        final ResourceNotFoundException excepcion =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.buscarPorId(
                                ID_CONSULTORIOS,
                                ID_USUARIOS
                        )
                );

        // Comprueba el código que utilizará el frontend.
        assertEquals(
                "USER_NOT_FOUND",
                excepcion.getCodigo()
        );

        assertEquals(
                "El usuario solicitado no existe.",
                excepcion.getMessage()
        );

        verify(
                usuarioRepository,
                times(1)
        ).findByIdUsuariosAndConsultorioIdConsultorios(
                ID_USUARIOS,
                ID_CONSULTORIOS
        );
    }

    @Test
    @DisplayName(
            "Debe rechazar un identificador de usuario inválido"
    )
    void debeRechazarIdUsuarioInvalido() {
        final IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.buscarPorId(
                                ID_CONSULTORIOS,
                                -1L
                        )
                );

        assertEquals(
                "El identificador del usuario no es válido.",
                excepcion.getMessage()
        );

        verify(
                usuarioRepository,
                never()
        ).findByIdUsuariosAndConsultorioIdConsultorios(
                any(),
                any()
        );
    }

    // Crea una solicitud reutilizable y válida.
    private static CrearUsuarioRequest crearSolicitudValida() {
        return new CrearUsuarioRequest(
                "MEDICO@CONSULTORIO.TEST",
                PASSWORD_ORIGINAL,
                "  Nombre Médico  ",
                "  Apellido Demo  ",
                " CMP-12345 ",
                " 999999999 "
        );
    }
}