package com.zenticode.medical.roles.service;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import com.zenticode.medical.roles.entity.Rol;
import com.zenticode.medical.roles.entity.UsuarioRol;
import com.zenticode.medical.roles.repository.RolRepository;
import com.zenticode.medical.roles.repository.UsuarioRolRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de AsignacionRolService.
 */
@ExtendWith(MockitoExtension.class)
class AsignacionRolServiceTest {

    private static final Long ID_CONSULTORIOS = 1L;
    private static final Long ID_USUARIOS = 10L;
    private static final Long ID_ROLES = 2L;

    @Mock
    private ConsultorioRepository consultorioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private Rol rol;

    private AsignacionRolService asignacionRolService;
    private Consultorio consultorio;
    private Usuario usuario;

    // Prepara los objetos reutilizados por las pruebas.
    @BeforeEach
    void configurar() {
        asignacionRolService = new AsignacionRolService(
                consultorioRepository,
                usuarioRepository,
                rolRepository,
                usuarioRolRepository
        );

        consultorio = new Consultorio(
                "consultorio-demo",
                "Consultorio Demo"
        );

        usuario = new Usuario(
                consultorio,
                "medico@consultorio.test",
                "$2a$12$hashSimulado",
                "Nombre Médico",
                "Apellido Demo"
        );
    }

    @Test
    @DisplayName(
            "Debe asignar un rol activo al usuario"
    )
    void debeAsignarRolActivo() {
        prepararConsultorioYUsuario();

        // Simula el rol activo solicitado.
        when(
                rolRepository.findByCodigoAndActivoTrue(
                        "ADMIN_CONSULTORIO"
                )
        ).thenReturn(Optional.of(rol));

        when(
                rol.getIdRoles()
        ).thenReturn(ID_ROLES);

        // Simula que el usuario todavía no tiene el rol.
        when(
                usuarioRolRepository
                        .existsByConsultorioIdConsultoriosAndUsuarioIdUsuariosAndRolIdRoles(
                                ID_CONSULTORIOS,
                                ID_USUARIOS,
                                ID_ROLES
                        )
        ).thenReturn(false);

        asignacionRolService.asignar(
                ID_CONSULTORIOS,
                ID_USUARIOS,
                " admin-consultorio "
        );

        final ArgumentCaptor<UsuarioRol> capturador =
                ArgumentCaptor.forClass(UsuarioRol.class);

        verify(
                usuarioRolRepository,
                times(1)
        ).save(capturador.capture());

        final UsuarioRol asignacionGuardada =
                capturador.getValue();

        assertSame(
                consultorio,
                asignacionGuardada.getConsultorio()
        );

        assertSame(
                usuario,
                asignacionGuardada.getUsuario()
        );

        assertSame(
                rol,
                asignacionGuardada.getRol()
        );

        // La asignación inicial no tiene asignador.
        assertNull(
                asignacionGuardada.getAsignadoPor()
        );

        // Confirma que el código fue normalizado.
        verify(
                rolRepository,
                times(1)
        ).findByCodigoAndActivoTrue(
                "ADMIN_CONSULTORIO"
        );
    }

    @Test
    @DisplayName(
            "Debe rechazar un rol ya asignado"
    )
    void debeRechazarRolDuplicado() {
        prepararConsultorioYUsuario();

        when(
                rolRepository.findByCodigoAndActivoTrue(
                        "MEDICO"
                )
        ).thenReturn(Optional.of(rol));

        when(
                rol.getIdRoles()
        ).thenReturn(ID_ROLES);

        when(
                usuarioRolRepository
                        .existsByConsultorioIdConsultoriosAndUsuarioIdUsuariosAndRolIdRoles(
                                ID_CONSULTORIOS,
                                ID_USUARIOS,
                                ID_ROLES
                        )
        ).thenReturn(true);

        final BusinessConflictException excepcion =
                assertThrows(
                        BusinessConflictException.class,
                        () -> asignacionRolService.asignar(
                                ID_CONSULTORIOS,
                                ID_USUARIOS,
                                "MEDICO"
                        )
                );

        assertEquals(
                "USER_ROLE_ALREADY_ASSIGNED",
                excepcion.getCodigo()
        );

        assertEquals(
                "El usuario ya tiene asignado este rol.",
                excepcion.getMessage()
        );

        // No debe guardar una asignación duplicada.
        verify(
                usuarioRolRepository,
                never()
        ).save(org.mockito.ArgumentMatchers.any(UsuarioRol.class));
    }

    @Test
    @DisplayName(
            "Debe rechazar un rol inexistente o desactivado"
    )
    void debeRechazarRolNoDisponible() {
        prepararConsultorioYUsuario();

        when(
                rolRepository.findByCodigoAndActivoTrue(
                        "ASISTENTE"
                )
        ).thenReturn(Optional.empty());

        final ResourceNotFoundException excepcion =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> asignacionRolService.asignar(
                                ID_CONSULTORIOS,
                                ID_USUARIOS,
                                "ASISTENTE"
                        )
                );

        assertEquals(
                "ROLE_NOT_FOUND",
                excepcion.getCodigo()
        );

        assertEquals(
                "El rol solicitado no existe o no está disponible.",
                excepcion.getMessage()
        );

        // No debe guardar si el rol no existe.
        verify(
                usuarioRolRepository,
                never()
        ).save(org.mockito.ArgumentMatchers.any(UsuarioRol.class));
    }

    @Test
    @DisplayName(
            "Debe rechazar usuarios fuera del consultorio"
    )
    void debeRechazarUsuarioFueraDelConsultorio() {
        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.of(consultorio));

        // No encuentra al usuario dentro de ese consultorio.
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
                        () -> asignacionRolService.asignar(
                                ID_CONSULTORIOS,
                                ID_USUARIOS,
                                "MEDICO"
                        )
                );

        assertEquals(
                "USER_NOT_FOUND",
                excepcion.getCodigo()
        );

        assertEquals(
                "El usuario solicitado no existe.",
                excepcion.getMessage()
        );

        // No debe buscar el rol si el usuario no es accesible.
        verify(
                rolRepository,
                never()
        ).findByCodigoAndActivoTrue(
                org.mockito.ArgumentMatchers.anyString()
        );

        // No debe guardar ninguna asignación.
        verify(
                usuarioRolRepository,
                never()
        ).save(org.mockito.ArgumentMatchers.any(UsuarioRol.class));
    }

    // Simula un consultorio activo y su usuario.
    private void prepararConsultorioYUsuario() {
        when(
                consultorioRepository
                        .findByIdConsultoriosAndEstado(
                                ID_CONSULTORIOS,
                                Consultorio.EstadoConsultorio.ACTIVO
                        )
        ).thenReturn(Optional.of(consultorio));

        when(
                usuarioRepository
                        .findByIdUsuariosAndConsultorioIdConsultorios(
                                ID_USUARIOS,
                                ID_CONSULTORIOS
                        )
        ).thenReturn(Optional.of(usuario));
    }
}