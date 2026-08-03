package com.zenticode.medical.shared.exception;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Representa la estructura uniforme de los errores devueltos por la API.
 *
 * <p>Todas las respuestas de error deberían mantener un formato estable.
 * Esto permite que el frontend pueda interpretar los errores sin depender
 * de mensajes internos de Spring Boot, Hibernate, PostgreSQL o Flyway.</p>
 *
 * <p>Ejemplo conceptual de respuesta:</p>
 *
 * <pre>
 * {
 *   "fecha": "2026-08-01T18:55:00-05:00",
 *   "estado": 400,
 *   "error": "Solicitud no válida",
 *   "codigo": "VALIDATION_ERROR",
 *   "mensaje": "Uno o más campos contienen errores.",
 *   "ruta": "/api/v1/consultorios",
 *   "erroresCampos": {
 *     "nombre": [
 *       "El nombre del consultorio es obligatorio."
 *     ]
 *   }
 * }
 * </pre>
 *
 * <p>Esta respuesta no debe contener:</p>
 *
 * <ul>
 *     <li>Stack traces.</li>
 *     <li>Consultas SQL.</li>
 *     <li>Nombres internos de tablas.</li>
 *     <li>Contraseñas o tokens.</li>
 *     <li>Rutas internas del servidor.</li>
 *     <li>Datos clínicos sensibles.</li>
 * </ul>
 *
 * <p>Se utiliza un {@code record} porque esta clase representa un resultado
 * inmutable que únicamente transporta información hacia el cliente.</p>
 *
 * @param fecha momento en el que se generó la respuesta
 * @param estado código numérico HTTP
 * @param error descripción general del tipo de error
 * @param codigo código estable utilizado por el frontend
 * @param mensaje explicación segura para el cliente
 * @param ruta ruta HTTP en la que ocurrió el error
 * @param erroresCampos errores asociados a campos concretos
 */
public record ApiErrorResponse(

        /**
         * Fecha y hora en la que se produjo el error.
         *
         * <p>Se conserva como un tipo temporal y no como texto para que
         * Jackson la convierta automáticamente a un formato ISO 8601.</p>
         */
        OffsetDateTime fecha,

        /**
         * Código HTTP numérico.
         *
         * <p>Ejemplos:</p>
         *
         * <ul>
         *     <li>400 para solicitudes no válidas.</li>
         *     <li>401 para solicitudes no autenticadas.</li>
         *     <li>403 para operaciones no autorizadas.</li>
         *     <li>404 para recursos inexistentes.</li>
         *     <li>409 para conflictos de negocio.</li>
         *     <li>500 para fallos internos inesperados.</li>
         * </ul>
         */
        int estado,

        /**
         * Descripción general y legible de la categoría del error.
         *
         * <p>Ejemplos: {@code Solicitud no válida},
         * {@code Recurso no encontrado} o {@code Conflicto}.</p>
         */
        String error,

        /**
         * Código interno estable que podrá interpretar el frontend.
         *
         * <p>El código no debe depender del texto del mensaje. Por ejemplo,
         * el frontend puede comprobar {@code VALIDATION_ERROR} aunque el
         * mensaje visible se modifique o traduzca posteriormente.</p>
         */
        String codigo,

        /**
         * Mensaje seguro que explica el problema de manera general.
         *
         * <p>Este valor nunca debe copiar directamente el mensaje de una
         * excepción de PostgreSQL, Hibernate o Spring.</p>
         */
        String mensaje,

        /**
         * Ruta HTTP que originó la respuesta.
         *
         * <p>Ejemplo: {@code /api/v1/consultorios}.</p>
         */
        String ruta,

        /**
         * Errores de validación asociados a campos concretos.
         *
         * <p>La clave representa el nombre del campo y el valor contiene uno
         * o varios mensajes. Se utiliza una lista porque un mismo campo podría
         * incumplir más de una validación.</p>
         *
         * <p>Cuando el error no pertenece a un campo específico, este mapa
         * será vacío.</p>
         */
        Map<String, List<String>> erroresCampos

) {

    /**
     * Constructor compacto encargado de comprobar y proteger la respuesta.
     *
     * <p>Además de validar datos obligatorios, crea una copia inmutable del
     * mapa de errores. Esto impide que otra parte de la aplicación modifique
     * la respuesta después de haberla construido.</p>
     */
    public ApiErrorResponse {
        fecha = Objects.requireNonNull(
                fecha,
                "La fecha del error es obligatoria."
        );

        if (estado < 400 || estado > 599) {
            throw new IllegalArgumentException(
                    "El estado HTTP de un error debe estar entre 400 y 599."
            );
        }

        error = validarTextoObligatorio(
                error,
                "La descripción general del error es obligatoria."
        );

        codigo = validarTextoObligatorio(
                codigo,
                "El código interno del error es obligatorio."
        );

        mensaje = validarTextoObligatorio(
                mensaje,
                "El mensaje del error es obligatorio."
        );

        ruta = normalizarRuta(ruta);
        erroresCampos = copiarErroresCampos(erroresCampos);
    }

    /**
     * Construye una respuesta para un error general sin errores de campos.
     *
     * <p>Este método se utilizará para situaciones como:</p>
     *
     * <ul>
     *     <li>Recurso no encontrado.</li>
     *     <li>Conflicto de negocio.</li>
     *     <li>Acceso no permitido.</li>
     *     <li>Error interno controlado.</li>
     * </ul>
     *
     * @param estado código HTTP
     * @param error categoría legible
     * @param codigo código interno estable
     * @param mensaje explicación segura
     * @param ruta ruta HTTP relacionada
     * @return respuesta de error sin errores de campos
     */
    public static ApiErrorResponse general(
            final int estado,
            final String error,
            final String codigo,
            final String mensaje,
            final String ruta
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                estado,
                error,
                codigo,
                mensaje,
                ruta,
                Collections.emptyMap()
        );
    }

    /**
     * Construye una respuesta para errores de validación de campos.
     *
     * <p>Ejemplo:</p>
     *
     * <pre>
     * nombre -> ["El nombre es obligatorio."]
     * ruc    -> ["El RUC debe tener 11 dígitos."]
     * </pre>
     *
     * @param estado código HTTP, normalmente 400
     * @param error categoría legible
     * @param codigo código interno estable
     * @param mensaje explicación general
     * @param ruta ruta HTTP relacionada
     * @param erroresCampos errores agrupados por campo
     * @return respuesta con el detalle de las validaciones
     */
    public static ApiErrorResponse conErroresCampos(
            final int estado,
            final String error,
            final String codigo,
            final String mensaje,
            final String ruta,
            final Map<String, List<String>> erroresCampos
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                estado,
                error,
                codigo,
                mensaje,
                ruta,
                erroresCampos
        );
    }

    /**
     * Comprueba que un texto requerido exista y no esté vacío.
     *
     * @param valor texto que se desea validar
     * @param mensajeError mensaje utilizado cuando el texto no es válido
     * @return texto sin espacios laterales
     */
    private static String validarTextoObligatorio(
            final String valor,
            final String mensajeError
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }

        return valor.trim();
    }

    /**
     * Normaliza la ruta HTTP.
     *
     * <p>Si no se proporciona una ruta, se utiliza {@code /}. El valor se
     * normaliza para comenzar siempre por una barra.</p>
     *
     * @param ruta ruta recibida
     * @return ruta normalizada
     */
    private static String normalizarRuta(final String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return "/";
        }

        final String rutaNormalizada = ruta.trim();

        if (rutaNormalizada.startsWith("/")) {
            return rutaNormalizada;
        }

        return "/" + rutaNormalizada;
    }

    /**
     * Crea una copia inmutable de los errores asociados a campos.
     *
     * <p>No se conserva directamente el mapa recibido porque el código que
     * lo construyó podría modificarlo posteriormente. La copia protege tanto
     * el mapa como cada lista de mensajes.</p>
     *
     * <p>Los campos o mensajes nulos no deberían producirse desde el
     * manejador global. Aun así, se ignoran para evitar respuestas
     * inconsistentes.</p>
     *
     * @param errores mapa de errores original
     * @return mapa inmutable y seguro
     */
    private static Map<String, List<String>> copiarErroresCampos(
            final Map<String, List<String>> errores
    ) {
        if (errores == null || errores.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, List<String>> copia =
                new LinkedHashMap<>();

        errores.forEach((campo, mensajes) -> {
            if (campo == null || campo.isBlank()) {
                return;
            }

            if (mensajes == null || mensajes.isEmpty()) {
                return;
            }

            final List<String> mensajesValidos = mensajes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(mensaje -> !mensaje.isBlank())
                    .distinct()
                    .toList();

            if (!mensajesValidos.isEmpty()) {
                copia.put(
                        campo.trim(),
                        List.copyOf(mensajesValidos)
                );
            }
        });

        return Collections.unmodifiableMap(copia);
    }
}