package com.zenticode.medical.consultorios.controller;

import com.zenticode.medical.consultorios.dto.ConsultorioResponse;
import com.zenticode.medical.consultorios.dto.CrearConsultorioRequest;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;
import com.zenticode.medical.consultorios.service.ConsultorioService;
import com.zenticode.medical.shared.exception.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de la capa HTTP de {@link ConsultorioController}.
 *
 * <p>Esta clase comprueba el comportamiento del controlador sin iniciar el
 * contexto completo de Spring Boot, PostgreSQL, Flyway, Testcontainers ni un
 * servidor Tomcat real.</p>
 *
 * <p>La estructura probada es:</p>
 *
 * <pre>
 * Solicitud HTTP simulada
 *          ↓
 * ConsultorioController real
 *          ↓
 * Jakarta Validation real
 *          ↓
 * ConsultorioService simulado
 *          ↓
 * Respuesta HTTP
 * </pre>
 *
 * <p>El servicio se sustituye mediante Mockito porque la lógica del servicio
 * ya está cubierta por {@code ConsultorioServiceTest}. Aquí nos interesa
 * comprobar exclusivamente:</p>
 *
 * <ul>
 *     <li>El contrato HTTP.</li>
 *     <li>El estado de respuesta.</li>
 *     <li>La cabecera Location.</li>
 *     <li>La serialización JSON.</li>
 *     <li>La validación del cuerpo.</li>
 *     <li>La integración con GlobalExceptionHandler.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ConsultorioControllerTest {

    /**
     * Simulación del servicio de consultorios.
     *
     * <p>El controlador utilizará este objeto como si fuera el servicio real,
     * pero no se ejecutará ninguna transacción ni operación sobre
     * PostgreSQL.</p>
     */
    @Mock
    private ConsultorioService consultorioService;

    /**
     * Herramienta de Spring utilizada para simular peticiones HTTP.
     */
    private MockMvc mockMvc;

    /**
     * Prepara el controlador y la infraestructura HTTP antes de cada prueba.
     *
     * <p>Se utiliza {@code standaloneSetup} porque solo necesitamos construir
     * la capa MVC alrededor del controlador que estamos probando.</p>
     *
     * <p>También registramos expresamente:</p>
     *
     * <ul>
     *     <li>El manejador global de excepciones.</li>
     *     <li>El validador Jakarta utilizado por @Valid.</li>
     * </ul>
     */
    /**
     * Prepara el controlador y la infraestructura HTTP antes de cada prueba.
     *
     * <p>Se utiliza {@code standaloneSetup} porque estas pruebas deben cargar
     * únicamente la capa MVC relacionada con {@link ConsultorioController}.
     * No necesitan iniciar Spring Boot completo, Flyway, PostgreSQL, Docker ni
     * el servidor Tomcat real.</p>
     *
     * <p>El servicio permanece simulado mediante Mockito. Sin embargo, la
     * validación de los DTO utiliza el motor real de Jakarta Validation,
     * adaptado a la interfaz de validación de Spring mediante
     * {@link LocalValidatorFactoryBean}.</p>
     *
     * <p>{@code LocalValidatorFactoryBean} implementa las dos integraciones
     * necesarias:</p>
     *
     * <ul>
     *     <li>La validación declarada mediante anotaciones de Jakarta, como
     *     {@code @NotBlank}, {@code @Size}, {@code @Email} y
     *     {@code @Pattern}.</li>
     *     <li>La interfaz {@code org.springframework.validation.Validator}
     *     requerida por MockMvc.</li>
     * </ul>
     *
     * <p>También se registra {@link GlobalExceptionHandler} para comprobar que
     * los errores producidos por {@code @Valid} sean convertidos al formato
     * uniforme {@code ApiErrorResponse} utilizado por la API.</p>
     */
    @BeforeEach
    void configurar() {
        /*
         * Creamos el controlador real e inyectamos el servicio simulado.
         *
         * De este modo se comprueba el comportamiento HTTP del controlador sin
         * ejecutar la lógica del servicio ni realizar operaciones en la base de
         * datos.
         */
        final ConsultorioController controller =
                new ConsultorioController(consultorioService);

        /*
         * LocalValidatorFactoryBean funciona como adaptador entre:
         *
         * Jakarta Validation
         *          ↓
         * Spring Validation
         *          ↓
         * MockMvc
         *
         * No utilizamos directamente jakarta.validation.Validator porque
         * MockMvcBuilders.setValidator(...) requiere específicamente la
         * interfaz org.springframework.validation.Validator.
         */
        final LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        /*
         * Inicializa internamente el proveedor de Jakarta Validation.
         *
         * En un contexto completo de Spring, este ciclo de vida sería gestionado
         * automáticamente por el contenedor. Como estamos construyendo MockMvc
         * manualmente mediante standaloneSetup, realizamos la inicialización de
         * forma explícita.
         */
        validator.afterPropertiesSet();

        /*
         * Construimos una infraestructura MVC aislada:
         *
         * 1. Registra ConsultorioController.
         * 2. Registra GlobalExceptionHandler.
         * 3. Registra el validador compatible con Spring.
         * 4. No inicia PostgreSQL, Flyway, Docker ni Tomcat.
         */
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .build();
    }

    /**
     * Comprueba la respuesta HTTP al crear correctamente un consultorio.
     *
     * <p>La prueba verifica que el controlador:</p>
     *
     * <ol>
     *     <li>Reciba el JSON.</li>
     *     <li>Lo convierta en CrearConsultorioRequest.</li>
     *     <li>Llame una sola vez al servicio.</li>
     *     <li>Devuelva HTTP 201.</li>
     *     <li>Incluya la cabecera Location.</li>
     *     <li>Devuelva el ConsultorioResponse como JSON.</li>
     * </ol>
     *
     * @throws Exception si MockMvc no puede ejecutar la petición
     */
    @Test
    @DisplayName(
            "Debe responder 201 y Location al crear un consultorio"
    )
    void debeResponderCreatedAlCrearConsultorio() throws Exception {
        final String codigoPublico =
                "716ffa2c-084e-4ddb-b396-c65caab28aaf";

        final OffsetDateTime fecha =
                OffsetDateTime.of(
                        2026,
                        8,
                        1,
                        19,
                        13,
                        56,
                        0,
                        ZoneOffset.ofHours(-5)
                );

        final ConsultorioResponse respuestaServicio =
                new ConsultorioResponse(
                        1L,
                        codigoPublico,
                        "Consultorio Médico Demo",
                        null,
                        "999999999",
                        "demo@consultorio.test",
                        "Dirección ficticia de demostración",
                        "America/Lima",
                        "PEN",
                        EstadoConsultorio.ACTIVO,
                        fecha,
                        fecha
                );

        /*
         * Cuando el controlador delegue la creación, el servicio simulado
         * devolverá una respuesta conocida y controlada.
         */
        when(
                consultorioService.crear(
                        any(CrearConsultorioRequest.class)
                )
        ).thenReturn(respuestaServicio);

        final String jsonValido = """
                {
                  "nombre": "Consultorio Médico Demo",
                  "telefono": "999999999",
                  "correo": "demo@consultorio.test",
                  "direccion": "Dirección ficticia de demostración",
                  "zonaHoraria": "America/Lima",
                  "moneda": "PEN"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/consultorios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(jsonValido)
                )
                /*
                 * Una creación correcta debe utilizar el estado HTTP 201.
                 */
                .andExpect(status().isCreated())

                /*
                 * La referencia pública debe aparecer en Location.
                 */
                .andExpect(
                        header().string(
                                "Location",
                                "http://localhost/api/v1/consultorios/"
                                        + codigoPublico
                        )
                )

                /*
                 * Comprobamos los campos principales del JSON.
                 */
                .andExpect(
                        jsonPath("$.idConsultorios")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.codigoPublico")
                                .value(codigoPublico)
                )
                .andExpect(
                        jsonPath("$.nombre")
                                .value("Consultorio Médico Demo")
                )
                .andExpect(
                        jsonPath("$.telefono")
                                .value("999999999")
                )
                .andExpect(
                        jsonPath("$.correo")
                                .value("demo@consultorio.test")
                )
                .andExpect(
                        jsonPath("$.zonaHoraria")
                                .value("America/Lima")
                )
                .andExpect(
                        jsonPath("$.moneda")
                                .value("PEN")
                )
                .andExpect(
                        jsonPath("$.estado")
                                .value("ACTIVO")
                );

        /*
         * El controlador debe delegar una sola vez. No debe repetir la
         * operación ni intentar guardar directamente.
         */
        verify(
                consultorioService,
                times(1)
        ).crear(any(CrearConsultorioRequest.class));
    }

    /**
     * Comprueba que un JSON con campos inválidos produzca HTTP 400.
     *
     * <p>La solicitud contiene los mismos tipos de errores que comprobamos
     * manualmente:</p>
     *
     * <ul>
     *     <li>Nombre vacío.</li>
     *     <li>RUC incompleto.</li>
     *     <li>Teléfono con letras.</li>
     *     <li>Correo sin formato válido.</li>
     *     <li>Moneda con más de tres letras.</li>
     * </ul>
     *
     * <p>El servicio no debe ejecutarse porque la validación del DTO ocurre
     * antes de entrar al método del controlador.</p>
     *
     * @throws Exception si MockMvc no puede ejecutar la petición
     */
    @Test
    @DisplayName(
            "Debe responder 400 y no llamar al servicio con datos inválidos"
    )
    void debeResponderBadRequestConDatosInvalidos() throws Exception {
        final String jsonInvalido = """
                {
                  "nombre": "",
                  "ruc": "123",
                  "telefono": "TELEFONO INVALIDO",
                  "correo": "correo-invalido",
                  "zonaHoraria": "America/Lima",
                  "moneda": "SOLES"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/consultorios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(jsonInvalido)
                )
                /*
                 * La validación debe producir HTTP 400.
                 */
                .andExpect(status().isBadRequest())

                /*
                 * GlobalExceptionHandler debe mantener el contrato uniforme.
                 */
                .andExpect(
                        jsonPath("$.estado")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Solicitud no válida")
                )
                .andExpect(
                        jsonPath("$.codigo")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.mensaje")
                                .value(
                                        "Uno o más campos contienen errores."
                                )
                )
                .andExpect(
                        jsonPath("$.ruta")
                                .value("/api/v1/consultorios")
                )

                /*
                 * Comprobamos la presencia de cada campo inválido.
                 *
                 * No dependemos del orden en el que Jakarta Validation
                 * devuelve las restricciones.
                 */
                .andExpect(
                        jsonPath("$.erroresCampos.nombre")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.ruc")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.telefono")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.correo")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.erroresCampos.moneda")
                                .isArray()
                );

        /*
         * Si @Valid rechazó la solicitud, el servicio nunca debe recibirla.
         * Esta comprobación evita operaciones de base de datos innecesarias.
         */
        verify(
                consultorioService,
                never()
        ).crear(any(CrearConsultorioRequest.class));
    }

    /**
     * Comprueba la respuesta cuando el cuerpo no es JSON válido.
     *
     * <p>Este caso debe ser transformado por
     * GlobalExceptionHandler a MALFORMED_JSON.</p>
     *
     * @throws Exception si MockMvc no puede ejecutar la petición
     */
    @Test
    @DisplayName(
            "Debe responder 400 cuando el JSON está mal formado"
    )
    void debeResponderBadRequestConJsonMalFormado() throws Exception {
        final String jsonMalFormado = """
                {
                  "nombre": "Consultorio incompleto",
                  "moneda": "PEN"
                """;

        mockMvc.perform(
                        post("/api/v1/consultorios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(jsonMalFormado)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.estado")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.codigo")
                                .value("MALFORMED_JSON")
                )
                .andExpect(
                        jsonPath("$.ruta")
                                .value("/api/v1/consultorios")
                );

        verify(
                consultorioService,
                never()
        ).crear(any(CrearConsultorioRequest.class));
    }
}