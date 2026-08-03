package com.zenticode.medical.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte excepciones de la API en respuestas seguras.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Registra errores técnicos únicamente en el servidor.
    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    // Maneja los errores producidos por @Valid en los DTO.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacionDto(
            final MethodArgumentNotValidException excepcion,
            final HttpServletRequest solicitud
    ) {
        final Map<String, List<String>> erroresCampos =
                new LinkedHashMap<>();

        for (FieldError errorCampo
                : excepcion
                .getBindingResult()
                .getFieldErrors()) {

            final String campo =
                    errorCampo.getField();

            final String mensaje =
                    errorCampo.getDefaultMessage() == null
                            ? "El valor proporcionado no es válido."
                            : errorCampo.getDefaultMessage();

            agregarErrorCampo(
                    erroresCampos,
                    campo,
                    mensaje
            );
        }

        final ApiErrorResponse respuesta =
                ApiErrorResponse.conErroresCampos(
                        HttpStatus.BAD_REQUEST.value(),
                        "Solicitud no válida",
                        "VALIDATION_ERROR",
                        "Uno o más campos contienen errores.",
                        obtenerRuta(solicitud),
                        erroresCampos
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Maneja restricciones aplicadas a parámetros o métodos.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarRestricciones(
            final ConstraintViolationException excepcion,
            final HttpServletRequest solicitud
    ) {
        final Map<String, List<String>> erroresCampos =
                new LinkedHashMap<>();

        for (ConstraintViolation<?> violacion
                : excepcion.getConstraintViolations()) {

            final String campo =
                    obtenerUltimoSegmento(
                            violacion
                                    .getPropertyPath()
                                    .toString()
                    );

            agregarErrorCampo(
                    erroresCampos,
                    campo,
                    violacion.getMessage()
            );
        }

        final ApiErrorResponse respuesta =
                ApiErrorResponse.conErroresCampos(
                        HttpStatus.BAD_REQUEST.value(),
                        "Solicitud no válida",
                        "CONSTRAINT_VIOLATION",
                        "Uno o más valores no cumplen "
                                + "las reglas requeridas.",
                        obtenerRuta(solicitud),
                        erroresCampos
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Maneja cuerpos JSON incompletos o mal formados.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarJsonNoLegible(
            final HttpMessageNotReadableException excepcion,
            final HttpServletRequest solicitud
    ) {
        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.BAD_REQUEST.value(),
                        "Solicitud no válida",
                        "MALFORMED_JSON",
                        "El cuerpo de la solicitud "
                                + "no tiene un formato válido.",
                        obtenerRuta(solicitud)
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Maneja parámetros con un tipo diferente al esperado.
    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ApiErrorResponse>
    manejarTipoParametroIncorrecto(
            final MethodArgumentTypeMismatchException excepcion,
            final HttpServletRequest solicitud
    ) {
        final String nombreParametro =
                excepcion.getName() == null
                        || excepcion.getName().isBlank()
                        ? "parámetro"
                        : excepcion.getName().trim();

        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parámetro no válido",
                        "PARAMETER_TYPE_MISMATCH",
                        "El parámetro '"
                                + nombreParametro
                                + "' no tiene el formato esperado.",
                        obtenerRuta(solicitud)
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Maneja argumentos inválidos detectados por el dominio.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse>
    manejarArgumentoInvalido(
            final IllegalArgumentException excepcion,
            final HttpServletRequest solicitud
    ) {
        final String mensaje =
                excepcion.getMessage() == null
                        || excepcion.getMessage().isBlank()
                        ? "Uno de los valores proporcionados "
                        + "no es válido."
                        : excepcion.getMessage().trim();

        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.BAD_REQUEST.value(),
                        "Solicitud no válida",
                        "INVALID_ARGUMENT",
                        mensaje,
                        obtenerRuta(solicitud)
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    // Convierte credenciales incorrectas en HTTP 401.
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse>
    manejarCredencialesInvalidas(
            final InvalidCredentialsException excepcion,
            final HttpServletRequest solicitud
    ) {
        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.UNAUTHORIZED.value(),
                        "No autorizado",
                        excepcion.getCodigo(),
                        excepcion.getMessage(),
                        obtenerRuta(solicitud)
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(respuesta);
    }

    // Convierte accesos sin permisos en HTTP 403.
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> manejarAccesoDenegado(
            final AuthorizationDeniedException excepcion,
            final HttpServletRequest solicitud
    ) {
        final String ruta =
                obtenerRuta(solicitud);

        // Registra el intento sin incluir datos clínicos.
        LOGGER.warn(
                "Acceso denegado a la ruta {}.",
                ruta
        );

        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.FORBIDDEN.value(),
                        "Acceso denegado",
                        "ACCESS_DENIED",
                        "No tiene permisos para realizar "
                                + "esta operación.",
                        ruta
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(respuesta);
    }

    // Convierte recursos inexistentes en HTTP 404.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    manejarRecursoNoEncontrado(
            final ResourceNotFoundException excepcion,
            final HttpServletRequest solicitud
    ) {
        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.NOT_FOUND.value(),
                        "Recurso no encontrado",
                        excepcion.getCodigo(),
                        excepcion.getMessage(),
                        obtenerRuta(solicitud)
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(respuesta);
    }

    // Convierte conflictos de negocio controlados en HTTP 409.
    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ApiErrorResponse>
    manejarConflictoNegocio(
            final BusinessConflictException excepcion,
            final HttpServletRequest solicitud
    ) {
        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.CONFLICT.value(),
                        "Conflicto",
                        excepcion.getCodigo(),
                        excepcion.getMessage(),
                        obtenerRuta(solicitud)
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta);
    }

    // Maneja conflictos detectados directamente por PostgreSQL.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse>
    manejarConflictoIntegridad(
            final DataIntegrityViolationException excepcion,
            final HttpServletRequest solicitud
    ) {
        final String ruta =
                obtenerRuta(solicitud);

        // Registra solo la clase de la causa, nunca el SQL.
        LOGGER.warn(
                "Conflicto de integridad en la ruta {}. Causa: {}",
                ruta,
                obtenerNombreCausaPrincipal(excepcion)
        );

        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.CONFLICT.value(),
                        "Conflicto",
                        "DATA_INTEGRITY_CONFLICT",
                        "La operación entra en conflicto "
                                + "con información existente.",
                        ruta
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta);
    }

    // Captura errores inesperados no gestionados anteriormente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    manejarErrorInesperado(
            final Exception excepcion,
            final HttpServletRequest solicitud
    ) {
        final String ruta =
                obtenerRuta(solicitud);

        // Guarda la excepción solo en los logs del servidor.
        LOGGER.error(
                "Error inesperado procesando la ruta {}.",
                ruta,
                excepcion
        );

        final ApiErrorResponse respuesta =
                ApiErrorResponse.general(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error interno",
                        "INTERNAL_ERROR",
                        "Ocurrió un error inesperado "
                                + "al procesar la solicitud.",
                        ruta
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta);
    }

    // Añade mensajes de validación evitando duplicados.
    private static void agregarErrorCampo(
            final Map<String, List<String>> errores,
            final String campo,
            final String mensaje
    ) {
        final String campoSeguro =
                campo == null || campo.isBlank()
                        ? "solicitud"
                        : campo.trim();

        final String mensajeSeguro =
                mensaje == null || mensaje.isBlank()
                        ? "El valor proporcionado no es válido."
                        : mensaje.trim();

        final List<String> mensajes =
                errores.computeIfAbsent(
                        campoSeguro,
                        clave -> new ArrayList<>()
                );

        if (!mensajes.contains(mensajeSeguro)) {
            mensajes.add(mensajeSeguro);
        }
    }

    // Obtiene el último elemento de una ruta de propiedad.
    private static String obtenerUltimoSegmento(
            final String rutaPropiedad
    ) {
        if (rutaPropiedad == null
                || rutaPropiedad.isBlank()) {
            return "solicitud";
        }

        final String rutaNormalizada =
                rutaPropiedad.trim();

        final int ultimoPunto =
                rutaNormalizada.lastIndexOf('.');

        if (ultimoPunto < 0
                || ultimoPunto
                == rutaNormalizada.length() - 1) {
            return rutaNormalizada;
        }

        return rutaNormalizada.substring(
                ultimoPunto + 1
        );
    }

    // Obtiene únicamente la URI sin parámetros de consulta.
    private static String obtenerRuta(
            final HttpServletRequest solicitud
    ) {
        if (solicitud == null
                || solicitud.getRequestURI() == null
                || solicitud.getRequestURI().isBlank()) {
            return "/";
        }

        return solicitud.getRequestURI();
    }

    // Obtiene la causa profunda sin exponer su mensaje.
    private static String obtenerNombreCausaPrincipal(
            final Throwable excepcion
    ) {
        if (excepcion == null) {
            return "UnknownCause";
        }

        Throwable causa =
                excepcion;

        while (causa.getCause() != null
                && causa.getCause() != causa) {
            causa = causa.getCause();
        }

        return causa
                .getClass()
                .getSimpleName();
    }
}