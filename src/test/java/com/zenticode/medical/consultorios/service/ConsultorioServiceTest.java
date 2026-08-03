package com.zenticode.medical.consultorios.service;

import com.zenticode.medical.consultorios.dto.ConsultorioResponse;
import com.zenticode.medical.consultorios.dto.CrearConsultorioRequest;
import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link ConsultorioService}.
 *
 * <p>Estas pruebas comprueban las reglas de aplicación del servicio sin
 * iniciar Spring Boot, Tomcat, Flyway ni PostgreSQL.</p>
 *
 * <p>El repositorio se sustituye por un objeto simulado de Mockito. Esto
 * permite comprobar:</p>
 *
 * <ul>
 *     <li>Qué entidad intenta guardar el servicio.</li>
 *     <li>Cuántas veces consulta la existencia del código público.</li>
 *     <li>Si evita guardar cuando existe un dato inválido.</li>
 *     <li>Qué valores predeterminados aplica.</li>
 * </ul>
 *
 * <p>Estas pruebas no sustituyen las pruebas de integración. Una prueba
 * posterior utilizará Testcontainers para ejecutar realmente PostgreSQL,
 * Flyway y las restricciones de la base.</p>
 */
@ExtendWith(MockitoExtension.class)
class ConsultorioServiceTest {

    /**
     * Simulación del repositorio.
     *
     * <p>Mockito crea este objeto durante cada prueba. No accede a una base
     * de datos real.</p>
     */
    @Mock
    private ConsultorioRepository consultorioRepository;

    /**
     * Servicio real que se desea probar.
     */
    private ConsultorioService consultorioService;

    /**
     * Prepara una instancia nueva del servicio antes de cada prueba.
     *
     * <p>Crear nuevamente el servicio evita que el estado de una prueba
     * influya sobre otra.</p>
     */
    @BeforeEach
    void configurar() {
        consultorioService = new ConsultorioService(
                consultorioRepository
        );
    }

    /**
     * Comprueba una creación con todos los datos administrativos.
     *
     * <p>La prueba verifica tanto la respuesta como la entidad enviada al
     * repositorio. Esto evita que un mapeo incorrecto pase desapercibido.</p>
     */
    @Test
    @DisplayName(
            "Debe crear un consultorio con todos los datos válidos"
    )
    void debeCrearConsultorioConDatosCompletos() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio Médico Demo",
                        "20123456789",
                        "+51 999 999 999",
                        "CONTACTO@CONSULTORIO.TEST",
                        "Dirección ficticia",
                        "America/Lima",
                        "PEN"
                );

        /*
         * Indicamos que cualquier UUID generado todavía no existe.
         */
        when(
                consultorioRepository.existsByCodigoPublico(
                        anyString()
                )
        ).thenReturn(false);

        /*
         * Simulamos que JPA devuelve la misma entidad recibida después de
         * guardarla.
         *
         * En esta prueba unitaria el identificador y las fechas permanecen
         * nulos porque PostgreSQL no participa. Eso será comprobado en la
         * prueba de integración.
         */
        when(
                consultorioRepository.save(any(Consultorio.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));

        final ConsultorioResponse respuesta =
                consultorioService.crear(solicitud);

        assertNotNull(respuesta);
        assertNotNull(respuesta.codigoPublico());

        /*
         * UUID.fromString lanza una excepción si el código no cumple
         * realmente el formato UUID.
         */
        assertDoesNotThrow(
                () -> UUID.fromString(respuesta.codigoPublico())
        );

        assertEquals(
                "Consultorio Médico Demo",
                respuesta.nombre()
        );
        assertEquals("20123456789", respuesta.ruc());
        assertEquals("+51 999 999 999", respuesta.telefono());

        /*
         * El DTO normaliza el correo a minúsculas.
         */
        assertEquals(
                "contacto@consultorio.test",
                respuesta.correo()
        );

        assertEquals(
                "Dirección ficticia",
                respuesta.direccion()
        );
        assertEquals(
                "America/Lima",
                respuesta.zonaHoraria()
        );
        assertEquals("PEN", respuesta.moneda());
        assertEquals(
                Consultorio.EstadoConsultorio.ACTIVO,
                respuesta.estado()
        );

        /*
         * En una prueba unitaria sin JPA real estas propiedades todavía no
         * son generadas por PostgreSQL.
         */
        assertNull(respuesta.idConsultorios());
        assertNull(respuesta.fechaCreacion());
        assertNull(respuesta.fechaModificacion());

        final ArgumentCaptor<Consultorio> capturador =
                ArgumentCaptor.forClass(Consultorio.class);

        verify(consultorioRepository).save(
                capturador.capture()
        );

        final Consultorio consultorioGuardado =
                capturador.getValue();

        assertEquals(
                solicitud.nombre(),
                consultorioGuardado.getNombre()
        );
        assertEquals(
                solicitud.ruc(),
                consultorioGuardado.getRuc()
        );
        assertEquals(
                solicitud.telefono(),
                consultorioGuardado.getTelefono()
        );
        assertEquals(
                solicitud.correo(),
                consultorioGuardado.getCorreo()
        );
        assertEquals(
                solicitud.direccion(),
                consultorioGuardado.getDireccion()
        );
        assertEquals(
                solicitud.zonaHoraria(),
                consultorioGuardado.getZonaHoraria()
        );
        assertEquals(
                solicitud.moneda(),
                consultorioGuardado.getMoneda()
        );

        verify(
                consultorioRepository,
                times(1)
        ).existsByCodigoPublico(anyString());

        verify(
                consultorioRepository,
                times(1)
        ).save(any(Consultorio.class));
    }

    /**
     * Comprueba los valores utilizados cuando zona horaria y moneda no se
     * proporcionan.
     */
    @Test
    @DisplayName(
            "Debe aplicar America/Lima y PEN como valores predeterminados"
    )
    void debeAplicarValoresPredeterminados() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio Predeterminado",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        when(
                consultorioRepository.existsByCodigoPublico(
                        anyString()
                )
        ).thenReturn(false);

        when(
                consultorioRepository.save(any(Consultorio.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));

        final ConsultorioResponse respuesta =
                consultorioService.crear(solicitud);

        assertEquals(
                "America/Lima",
                respuesta.zonaHoraria()
        );
        assertEquals("PEN", respuesta.moneda());
        assertEquals(
                Consultorio.EstadoConsultorio.ACTIVO,
                respuesta.estado()
        );

        assertNull(respuesta.ruc());
        assertNull(respuesta.telefono());
        assertNull(respuesta.correo());
        assertNull(respuesta.direccion());

        verify(
                consultorioRepository,
                times(1)
        ).save(any(Consultorio.class));
    }

    /**
     * Comprueba que una solicitud nula no llegue al repositorio.
     */
    @Test
    @DisplayName(
            "Debe rechazar una solicitud nula sin guardar"
    )
    void debeRechazarSolicitudNula() {
        final NullPointerException excepcion =
                assertThrows(
                        NullPointerException.class,
                        () -> consultorioService.crear(null)
                );

        assertEquals(
                "La solicitud de creación es obligatoria.",
                excepcion.getMessage()
        );

        verify(
                consultorioRepository,
                never()
        ).existsByCodigoPublico(anyString());

        verify(
                consultorioRepository,
                never()
        ).save(any(Consultorio.class));
    }

    /**
     * Comprueba que una zona con apariencia correcta, pero inexistente, sea
     * rechazada por la capa de servicio.
     */
    @Test
    @DisplayName(
            "Debe rechazar una zona horaria inexistente"
    )
    void debeRechazarZonaHorariaInexistente() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio Zona Inválida",
                        null,
                        null,
                        null,
                        null,
                        "America/ZonaInventada",
                        "PEN"
                );

        final IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> consultorioService.crear(solicitud)
                );

        assertEquals(
                "La zona horaria proporcionada no es válida.",
                excepcion.getMessage()
        );

        assertNotNull(excepcion.getCause());
        assertInstanceOf(
                java.time.DateTimeException.class,
                excepcion.getCause()
        );

        verify(
                consultorioRepository,
                never()
        ).existsByCodigoPublico(anyString());

        verify(
                consultorioRepository,
                never()
        ).save(any(Consultorio.class));
    }

    /**
     * Comprueba que un código de tres letras no sea aceptado solamente por
     * tener la longitud correcta.
     *
     * <p>{@code XYZ} respeta el formato del DTO, pero no representa una
     * moneda reconocida por {@link java.util.Currency}.</p>
     */
    @Test
    @DisplayName(
            "Debe rechazar una moneda no reconocida"
    )
    void debeRechazarMonedaNoReconocida() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio Moneda Inválida",
                        null,
                        null,
                        null,
                        null,
                        "America/Lima",
                        "XYZ"
                );

        final IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> consultorioService.crear(solicitud)
                );

        assertEquals(
                "La moneda proporcionada no es válida.",
                excepcion.getMessage()
        );

        assertNotNull(excepcion.getCause());

        verify(
                consultorioRepository,
                never()
        ).existsByCodigoPublico(anyString());

        verify(
                consultorioRepository,
                never()
        ).save(any(Consultorio.class));
    }

    /**
     * Comprueba que el servicio vuelva a generar un UUID cuando el primer
     * candidato ya aparece registrado.
     *
     * <p>No podemos controlar directamente los valores producidos por
     * {@link UUID#randomUUID()}, pero sí podemos simular que:</p>
     *
     * <ol>
     *     <li>El primer candidato ya existe.</li>
     *     <li>El segundo candidato está disponible.</li>
     * </ol>
     */
    @Test
    @DisplayName(
            "Debe reintentar cuando el primer código público ya existe"
    )
    void debeReintentarCodigoPublicoDuplicado() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio con Reintento",
                        null,
                        null,
                        null,
                        null,
                        "America/Lima",
                        "PEN"
                );

        when(
                consultorioRepository.existsByCodigoPublico(
                        anyString()
                )
        ).thenReturn(true, false);

        when(
                consultorioRepository.save(any(Consultorio.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));

        final ConsultorioResponse respuesta =
                consultorioService.crear(solicitud);

        assertNotNull(respuesta.codigoPublico());
        assertDoesNotThrow(
                () -> UUID.fromString(respuesta.codigoPublico())
        );

        verify(
                consultorioRepository,
                times(2)
        ).existsByCodigoPublico(anyString());

        verify(
                consultorioRepository,
                times(1)
        ).save(any(Consultorio.class));
    }

    /**
     * Comprueba el límite de seguridad de cinco intentos.
     *
     * <p>Si todos los UUID candidatos aparecen ocupados, el servicio debe
     * fallar de forma controlada y nunca intentar guardar una entidad con
     * un código no confirmado.</p>
     */
    @Test
    @DisplayName(
            "Debe fallar después de cinco códigos públicos ocupados"
    )
    void debeFallarCuandoNoPuedeGenerarCodigoPublico() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio sin Código",
                        null,
                        null,
                        null,
                        null,
                        "America/Lima",
                        "PEN"
                );

        when(
                consultorioRepository.existsByCodigoPublico(
                        anyString()
                )
        ).thenReturn(true);

        final IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> consultorioService.crear(solicitud)
                );

        assertEquals(
                "No fue posible generar un código público único.",
                excepcion.getMessage()
        );

        verify(
                consultorioRepository,
                times(5)
        ).existsByCodigoPublico(anyString());

        verify(
                consultorioRepository,
                never()
        ).save(any(Consultorio.class));
    }

    /**
     * Comprobación adicional de que el UUID generado no sea una cadena vacía.
     */
    @Test
    @DisplayName(
            "El código público generado debe ser un UUID no vacío"
    )
    void codigoPublicoDebeSerUuidNoVacio() {
        final CrearConsultorioRequest solicitud =
                new CrearConsultorioRequest(
                        "Consultorio UUID",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        when(
                consultorioRepository.existsByCodigoPublico(
                        anyString()
                )
        ).thenReturn(false);

        when(
                consultorioRepository.save(any(Consultorio.class))
        ).thenAnswer(invocacion -> invocacion.getArgument(0));

        final ConsultorioResponse respuesta =
                consultorioService.crear(solicitud);

        assertNotNull(respuesta.codigoPublico());
        assertFalse(respuesta.codigoPublico().isBlank());
        assertEquals(
                36,
                respuesta.codigoPublico().length()
        );

        final UUID uuid =
                UUID.fromString(respuesta.codigoPublico());

        assertNotNull(uuid);
        assertTrue(
                respuesta.codigoPublico().contains("-")
        );
    }
}