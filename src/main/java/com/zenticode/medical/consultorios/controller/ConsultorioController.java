package com.zenticode.medical.consultorios.controller;

import com.zenticode.medical.consultorios.dto.ConsultorioResponse;
import com.zenticode.medical.consultorios.dto.CrearConsultorioRequest;
import com.zenticode.medical.consultorios.service.ConsultorioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

/**
 * Controlador REST encargado de exponer los casos de uso relacionados con
 * consultorios.
 *
 * <p>Este controlador representa la entrada HTTP del módulo. Su
 * responsabilidad es recibir solicitudes, activar las validaciones del DTO,
 * delegar la operación a {@link ConsultorioService} y construir una respuesta
 * HTTP adecuada.</p>
 *
 * <p>El controlador no debe:</p>
 *
 * <ul>
 *     <li>Acceder directamente al repositorio.</li>
 *     <li>Construir entidades JPA.</li>
 *     <li>Generar códigos públicos.</li>
 *     <li>Validar manualmente zonas horarias o monedas.</li>
 *     <li>Gestionar transacciones.</li>
 *     <li>Incluir reglas clínicas o administrativas.</li>
 * </ul>
 *
 * <p>La ruta base utiliza versionado:</p>
 *
 * <pre>
 * /api/v1/consultorios
 * </pre>
 *
 * <p>El segmento {@code v1} permitirá evolucionar el contrato HTTP en el
 * futuro sin romper inmediatamente clientes que utilicen una versión
 * anterior.</p>
 */
@RestController
@RequestMapping("/api/v1/consultorios")
public class ConsultorioController {

    /**
     * Servicio de aplicación que contiene el caso de uso de creación.
     */
    private final ConsultorioService consultorioService;

    /**
     * Constructor utilizado por Spring para inyectar el servicio.
     *
     * <p>Se utiliza inyección por constructor porque la dependencia es
     * obligatoria, puede mantenerse como {@code final} y resulta sencilla de
     * sustituir durante las pruebas unitarias del controlador.</p>
     *
     * @param consultorioService servicio de consultorios
     */
    public ConsultorioController(
            final ConsultorioService consultorioService
    ) {
        this.consultorioService = Objects.requireNonNull(
                consultorioService,
                "El servicio de consultorios es obligatorio."
        );
    }

    /**
     * Crea un nuevo consultorio.
     *
     * <p>Ruta:</p>
     *
     * <pre>
     * POST /api/v1/consultorios
     * </pre>
     *
     * <p>La anotación {@link Valid} activa las restricciones declaradas en
     * {@link CrearConsultorioRequest}. Si la solicitud contiene datos
     * inválidos, Spring lanza una excepción que será transformada por el
     * manejador global en un {@code ApiErrorResponse}.</p>
     *
     * <p>La anotación {@link RequestBody} indica que el parámetro se construye
     * a partir del JSON enviado en el cuerpo HTTP.</p>
     *
     * <p>Una creación correcta devuelve:</p>
     *
     * <ul>
     *     <li>Estado HTTP {@code 201 Created}.</li>
     *     <li>Cabecera {@code Location} con la referencia pública.</li>
     *     <li>Un {@link ConsultorioResponse} en el cuerpo.</li>
     * </ul>
     *
     * <p>La cabecera {@code Location} tendrá una forma equivalente a:</p>
     *
     * <pre>
     * /api/v1/consultorios/{codigoPublico}
     * </pre>
     *
     * <p>El código público se utiliza en lugar de la clave primaria
     * secuencial para evitar que las referencias externas dependan de
     * {@code id_consultorios}.</p>
     *
     * @param solicitud datos recibidos y validados
     * @return respuesta HTTP 201 con el consultorio creado
     */
    @PostMapping
    public ResponseEntity<ConsultorioResponse> crear(
            @Valid
            @RequestBody
            final CrearConsultorioRequest solicitud
    ) {
        /*
         * El controlador delega toda la lógica al servicio.
         *
         * El servicio:
         * 1. Resuelve la zona horaria.
         * 2. Resuelve la moneda.
         * 3. Genera el UUID público.
         * 4. Construye la entidad.
         * 5. Persiste dentro de una transacción.
         * 6. Devuelve un DTO de respuesta.
         */
        final ConsultorioResponse respuesta =
                consultorioService.crear(solicitud);

        /*
         * Construimos la URI a partir de la solicitud HTTP actual.
         *
         * Si la ruta actual es:
         * /api/v1/consultorios
         *
         * la URI resultante será:
         * /api/v1/consultorios/{codigoPublico}
         */
        final URI ubicacion =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{codigoPublico}")
                        .buildAndExpand(
                                respuesta.codigoPublico()
                        )
                        .toUri();

        /*
         * ResponseEntity.created(...) establece:
         *
         * HTTP 201 Created
         * Location: /api/v1/consultorios/{codigoPublico}
         *
         * body(...) añade el DTO creado al cuerpo de la respuesta.
         */
        return ResponseEntity
                .created(ubicacion)
                .body(respuesta);
    }
}