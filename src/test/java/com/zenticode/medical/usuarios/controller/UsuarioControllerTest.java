package com.zenticode.medical.usuarios.controller;

import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.GlobalExceptionHandler;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import com.zenticode.medical.usuarios.dto.CrearUsuarioRequest;
import com.zenticode.medical.usuarios.dto.UsuarioResponse;
import com.zenticode.medical.usuarios.entity.Usuario.EstadoUsuario;
import com.zenticode.medical.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba el contrato HTTP del controlador de usuarios.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private static final Long ID_CONSULTORIOS = 1L;
    private static final Long ID_USUARIOS = 10L;

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    // Configura MockMvc sin iniciar Spring Boot completo.
    @BeforeEach
    void configurar() {
        final UsuarioController controller =
                new UsuarioController(usuarioService);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName(
            "Debe responder 201 y Location al crear un usuario"
    )
    void debeResponderCreatedAlCrearUsuario() throws Exception {
        final UsuarioResponse respuestaServicio =
                crearRespuestaValida();

        when(
                usuarioService.crear(
                        eq(ID_CONSULTORIOS),
                        any(CrearUsuarioRequest.class)
                )
        ).thenReturn(respuestaServicio);

        final String jsonValido = """
                {
                  "correo": "doctor.demo@zenticode.pe",
                  "password": "ZentiDemo2026!",
                  "nombres": "Doctor",
                  "apellidos": "Demostración",
                  "numeroColegiatura": "CMP-12345",
                  "telefono": "999999999"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/consultorios/{idConsultorios}"
                                        + "/usuarios",
                                ID_CONSULTORIOS
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(jsonValido)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "http://localhost/api/v1/consultorios/"
                                        + ID_CONSULTORIOS
                                        + "/usuarios/"
                                        + ID_USUARIOS
                        )
                )
                .andExpect(
                        jsonPath("$.idUsuarios")
                                .value(ID_USUARIOS)
                )
                .andExpect(
                        jsonPath("$.idConsultorios")
                                .value(ID_CONSULTORIOS)
                )
                .andExpect(
                        jsonPath("$.correo")
                                .value("medico@consultorio.test")
                )
                .andExpect(
                        jsonPath("$.nombres")
                                .value("Nombre Médico")
                )
                .andExpect(
                        jsonPath("$.apellidos")
                                .value("Apellido Demo")
                )
                .andExpect(
                        jsonPath("$.numeroColegiatura")
                                .value("CMP-12345")
                )
                .andExpect(
                        jsonPath("$.telefono")
                                .value("999999999")
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("ACTIVO")
                )
                // Comprueba que no se exponga la contraseña.
                .andExpect(
                        jsonPath("$", not(hasKey("password")))
                )
                // Comprueba que no se exponga el hash.
                .andExpect(
                        jsonPath("$", not(hasKey("passwordHash")))
                )
                // Comprueba que no se expongan intentos fallidos.
                .andExpect(
                        jsonPath("$", not(hasKey("intentosFallidos")))
                )
                // Comprueba que no se exponga el bloqueo temporal.
                .andExpect(
                        jsonPath("$", not(hasKey("bloqueadoHasta")))
                );

        verify(
                usuarioService,
                times(1)
        ).crear(
                eq(ID_CONSULTORIOS),
                any(CrearUsuarioRequest.class)
        );
    }

    @Test
    @DisplayName(
            "Debe responder 200 al buscar un usuario"
    )
    void debeResponderOkAlBuscarUsuario() throws Exception {
        when(
                usuarioService.buscarPorId(
                        ID_CONSULTORIOS,
                        ID_USUARIOS
                )
        ).thenReturn(crearRespuestaValida());

        mockMvc.perform(
                        get(
                                "/api/v1/consultorios/{idConsultorios}"
                                        + "/usuarios/{idUsuarios}",
                                ID_CONSULTORIOS,
                                ID_USUARIOS
                        )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.idUsuarios")
                                .value(ID_USUARIOS)
                )
                .andExpect(
                        jsonPath("$.idConsultorios")
                                .value(ID_CONSULTORIOS)
                )
                .andExpect(
                        jsonPath("$.correo")
                                .value("medico@consultorio.test")
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("ACTIVO")
                )
                .andExpect(
                        jsonPath("$", not(hasKey("passwordHash")))
                );

        verify(
                usuarioService,
                times(1)
        ).buscarPorId(
                ID_CONSULTORIOS,
                ID_USUARIOS
        );
    }

    @Test
    @DisplayName(
            "Debe responder 400 con datos de usuario inválidos"
    )
    void debeResponderBadRequestConDatosInvalidos()
            throws Exception {

        final String jsonInvalido = """
                {
                  "correo": "correo-invalido",
                  "password": "corta",
                  "nombres": "",
                  "apellidos": "",
                  "numeroColegiatura": "CMP#INVALIDO",
                  "telefono": "TELEFONO"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/consultorios/{idConsultorios}"
                                        + "/usuarios",
                                ID_CONSULTORIOS
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(jsonInvalido)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.estado")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.codigo")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.ruta")
                                .value(
                                        "/api/v1/consultorios/1/usuarios"
                                )
                )
                .andExpect(
                        jsonPath("$.erroresCampos.correo")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.password")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.nombres")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.apellidos")
                                .isArray()
                )
                .andExpect(
                        jsonPath(
                                "$.erroresCampos.numeroColegiatura"
                        ).isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.telefono")
                                .isArray()
                );

        // El servicio no debe recibir solicitudes inválidas.
        verify(
                usuarioService,
                never()
        ).crear(
                eq(ID_CONSULTORIOS),
                any(CrearUsuarioRequest.class)
        );
    }

    @Test
    @DisplayName(
            "Debe responder 409 cuando el correo ya existe"
    )
    void debeResponderConflictConCorreoDuplicado()
            throws Exception {

        when(
                usuarioService.crear(
                        eq(ID_CONSULTORIOS),
                        any(CrearUsuarioRequest.class)
                )
        ).thenThrow(
                new BusinessConflictException(
                        "USER_EMAIL_ALREADY_EXISTS",
                        "El correo ya está registrado en el consultorio."
                )
        );

        final String jsonValido = """
                {
                  "correo": "medico@consultorio.test",
                  "password": "FraseSegura2026",
                  "nombres": "Nombre Médico",
                  "apellidos": "Apellido Demo",
                  "numeroColegiatura": "CMP-12345",
                  "telefono": "999999999"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/consultorios/{idConsultorios}"
                                        + "/usuarios",
                                ID_CONSULTORIOS
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(jsonValido)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.estado")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflicto")
                )
                .andExpect(
                        jsonPath("$.codigo")
                                .value(
                                        "USER_EMAIL_ALREADY_EXISTS"
                                )
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "El correo ya está registrado "
                                                + "en el consultorio."
                                )
                )
                .andExpect(
                        jsonPath("$.ruta")
                                .value(
                                        "/api/v1/consultorios/1/usuarios"
                                )
                );
    }

    @Test
    @DisplayName(
            "Debe responder 404 cuando el usuario no existe"
    )
    void debeResponderNotFoundConUsuarioInexistente()
            throws Exception {

        when(
                usuarioService.buscarPorId(
                        ID_CONSULTORIOS,
                        ID_USUARIOS
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "El usuario solicitado no existe."
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/consultorios/{idConsultorios}"
                                        + "/usuarios/{idUsuarios}",
                                ID_CONSULTORIOS,
                                ID_USUARIOS
                        )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.estado")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Recurso no encontrado")
                )
                .andExpect(
                        jsonPath("$.codigo")
                                .value("USER_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "El usuario solicitado no existe."
                                )
                )
                .andExpect(
                        jsonPath("$.ruta")
                                .value(
                                        "/api/v1/consultorios/1"
                                                + "/usuarios/10"
                                )
                );
    }

    // Crea una respuesta reutilizable para las pruebas.
    private static UsuarioResponse crearRespuestaValida() {
        final OffsetDateTime fecha =
                OffsetDateTime.of(
                        2026,
                        8,
                        2,
                        11,
                        45,
                        0,
                        0,
                        ZoneOffset.ofHours(-5)
                );

        return new UsuarioResponse(
                ID_USUARIOS,
                ID_CONSULTORIOS,
                "medico@consultorio.test",
                "Nombre Médico",
                "Apellido Demo",
                "CMP-12345",
                "999999999",
                EstadoUsuario.ACTIVO,
                null,
                fecha,
                fecha
        );
    }
}