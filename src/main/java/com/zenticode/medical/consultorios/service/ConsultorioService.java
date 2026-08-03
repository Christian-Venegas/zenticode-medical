package com.zenticode.medical.consultorios.service;

import com.zenticode.medical.consultorios.dto.ConsultorioResponse;
import com.zenticode.medical.consultorios.dto.CrearConsultorioRequest;
import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Currency;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de aplicación encargado de gestionar los casos de uso
 * relacionados con consultorios.
 *
 * <p>Esta clase se encuentra entre el futuro controlador REST y el
 * repositorio de persistencia:</p>
 *
 * <pre>
 * Controlador REST
 *        ↓
 * ConsultorioService
 *        ↓
 * ConsultorioRepository
 *        ↓
 * PostgreSQL
 * </pre>
 *
 * <p>El servicio concentra las reglas necesarias para crear correctamente
 * un consultorio. El controlador no deberá generar códigos, validar zonas
 * horarias ni construir entidades JPA directamente.</p>
 *
 * <p>La anotación {@link Service} permite que Spring detecte esta clase y
 * administre su ciclo de vida mediante el contenedor de dependencias.</p>
 */
@Service
public class ConsultorioService {

    /**
     * Zona horaria utilizada cuando el cliente no proporciona una.
     */
    private static final String ZONA_HORARIA_PREDETERMINADA =
            "America/Lima";

    /**
     * Moneda utilizada cuando el cliente no proporciona una.
     */
    private static final String MONEDA_PREDETERMINADA = "PEN";

    /**
     * Repositorio utilizado para acceder a la tabla
     * {@code consultorios}.
     */
    private final ConsultorioRepository consultorioRepository;

    /**
     * Constructor explícito utilizado por Spring para inyectar el
     * repositorio.
     *
     * <p>Se utiliza inyección por constructor porque:</p>
     *
     * <ul>
     *     <li>La dependencia es obligatoria.</li>
     *     <li>El atributo puede mantenerse como {@code final}.</li>
     *     <li>Facilita las pruebas unitarias.</li>
     *     <li>Evita inyección oculta sobre campos.</li>
     * </ul>
     *
     * @param consultorioRepository repositorio de consultorios
     */
    public ConsultorioService(
            final ConsultorioRepository consultorioRepository
    ) {
        this.consultorioRepository = Objects.requireNonNull(
                consultorioRepository,
                "El repositorio de consultorios es obligatorio."
        );
    }

    /**
     * Registra un nuevo consultorio.
     *
     * <p>El flujo de creación es el siguiente:</p>
     *
     * <ol>
     *     <li>Comprobar que la solicitud existe.</li>
     *     <li>Determinar y validar la zona horaria.</li>
     *     <li>Determinar y validar la moneda.</li>
     *     <li>Generar un código público no secuencial.</li>
     *     <li>Construir la entidad de dominio.</li>
     *     <li>Aplicar los datos administrativos opcionales.</li>
     *     <li>Guardar el registro dentro de una transacción.</li>
     *     <li>Convertir la entidad guardada en una respuesta segura.</li>
     * </ol>
     *
     * <p>La transacción garantiza que la operación se complete totalmente o
     * se revierta. Si PostgreSQL rechaza el registro, la transacción no deja
     * un consultorio parcialmente almacenado.</p>
     *
     * @param solicitud datos validados de entrada
     * @return información del consultorio creado
     * @throws NullPointerException si la solicitud es nula
     * @throws IllegalArgumentException si la zona horaria o moneda no son
     *                                  válidas
     */
    @Transactional
    public ConsultorioResponse crear(
            final CrearConsultorioRequest solicitud
    ) {
        Objects.requireNonNull(
                solicitud,
                "La solicitud de creación es obligatoria."
        );

        final String zonaHoraria = resolverZonaHoraria(
                solicitud.zonaHoraria()
        );

        final String moneda = resolverMoneda(
                solicitud.moneda()
        );

        final String codigoPublico = generarCodigoPublicoUnico();

        final Consultorio consultorio = new Consultorio(
                codigoPublico,
                solicitud.nombre()
        );

        consultorio.actualizarDatos(
                solicitud.nombre(),
                solicitud.ruc(),
                solicitud.telefono(),
                solicitud.correo(),
                solicitud.direccion(),
                zonaHoraria,
                moneda
        );

        final Consultorio consultorioGuardado =
                consultorioRepository.save(consultorio);

        /*
         * saveAndFlush() no se utiliza de forma general porque forzar un
         * flush temprano puede perjudicar el agrupamiento de operaciones.
         *
         * En este caso, la transacción realizará el flush antes del commit.
         * Las pruebas de integración comprobarán las restricciones reales
         * de PostgreSQL.
         */
        return ConsultorioResponse.desde(consultorioGuardado);
    }

    /**
     * Determina la zona horaria que se utilizará y comprueba que exista en
     * el catálogo de Java.
     *
     * <p>No se utiliza únicamente una expresión regular porque un texto
     * puede tener apariencia válida y no representar una zona real. Por
     * ejemplo, {@code America/ZonaInventada} podría respetar el formato,
     * pero no existe en el catálogo IANA utilizado por Java.</p>
     *
     * @param zonaHoraria zona recibida desde la solicitud
     * @return identificador normalizado y válido
     * @throws IllegalArgumentException si la zona no existe
     */
    private String resolverZonaHoraria(final String zonaHoraria) {
        final String zonaResuelta =
                zonaHoraria == null
                        ? ZONA_HORARIA_PREDETERMINADA
                        : zonaHoraria;

        try {
            /*
             * ZoneId.of valida el identificador y lanza una excepción cuando
             * no existe. getId devuelve la representación normalizada.
             */
            return ZoneId.of(zonaResuelta).getId();
        } catch (DateTimeException excepcion) {
            throw new IllegalArgumentException(
                    "La zona horaria proporcionada no es válida.",
                    excepcion
            );
        }
    }

    /**
     * Determina y valida la moneda del consultorio.
     *
     * <p>El DTO garantiza tres letras, pero este método comprueba además que
     * el código corresponda a una moneda reconocida por Java.</p>
     *
     * <p>El valor se normaliza a mayúsculas antes de validarlo. Si no se
     * proporciona moneda, se utiliza {@code PEN}.</p>
     *
     * @param moneda código recibido desde la solicitud
     * @return código de moneda válido en mayúsculas
     * @throws IllegalArgumentException si el código no es reconocido
     */
    private String resolverMoneda(final String moneda) {
        final String monedaResuelta =
                moneda == null
                        ? MONEDA_PREDETERMINADA
                        : moneda.toUpperCase(Locale.ROOT);

        try {
            return Currency
                    .getInstance(monedaResuelta)
                    .getCurrencyCode();
        } catch (IllegalArgumentException excepcion) {
            throw new IllegalArgumentException(
                    "La moneda proporcionada no es válida.",
                    excepcion
            );
        }
    }

    /**
     * Genera un identificador público único para el consultorio.
     *
     * <p>Se utiliza un UUID aleatorio porque:</p>
     *
     * <ul>
     *     <li>No revela cuántos consultorios existen.</li>
     *     <li>No depende de la clave primaria secuencial.</li>
     *     <li>Puede utilizarse en referencias externas.</li>
     *     <li>La longitud coincide con {@code codigo_publico VARCHAR(36)}.</li>
     * </ul>
     *
     * <p>La probabilidad de colisión de UUID es extremadamente pequeña, pero
     * la aplicación comprueba igualmente la existencia y PostgreSQL mantiene
     * una restricción única como defensa definitiva.</p>
     *
     * @return código público único en formato UUID
     * @throws IllegalStateException si no pudiera obtenerse un código libre
     *                               después de varios intentos
     */
    private String generarCodigoPublicoUnico() {
        /*
         * El límite evita un bucle infinito si el repositorio o la base
         * presentaran un comportamiento inesperado.
         */
        final int maximoIntentos = 5;

        for (int intento = 1; intento <= maximoIntentos; intento++) {
            final String codigoCandidato = UUID.randomUUID().toString();

            if (!consultorioRepository.existsByCodigoPublico(
                    codigoCandidato
            )) {
                return codigoCandidato;
            }
        }

        throw new IllegalStateException(
                "No fue posible generar un código público único."
        );
    }
}