package com.zenticode.medical.consultorios.dto;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Representa la información pública de un consultorio que puede enviarse
 * como respuesta desde la API REST.
 *
 * <p>Este DTO evita devolver directamente la entidad JPA
 * {@link Consultorio}. Separar la respuesta HTTP de la entidad de
 * persistencia proporciona varias ventajas:</p>
 *
 * <ul>
 *     <li>Evita exponer campos internos accidentalmente.</li>
 *     <li>Impide depender de la estructura exacta de PostgreSQL.</li>
 *     <li>Permite modificar la entidad sin romper el contrato de la API.</li>
 *     <li>Evita problemas futuros de serialización de relaciones JPA.</li>
 *     <li>Define expresamente qué información puede recibir el frontend.</li>
 * </ul>
 *
 * <p>En esta primera versión se incluye la clave primaria porque será
 * necesaria internamente para desarrollar y relacionar módulos. En las
 * operaciones externas se priorizará el uso de {@code codigoPublico} para
 * no depender de identificadores secuenciales en URLs públicas.</p>
 *
 * @param idConsultorios clave primaria interna del consultorio
 * @param codigoPublico identificador público no secuencial
 * @param nombre nombre visible del consultorio
 * @param ruc Registro Único de Contribuyentes, cuando exista
 * @param telefono teléfono administrativo
 * @param correo correo administrativo
 * @param direccion dirección o referencia física
 * @param zonaHoraria zona horaria IANA utilizada por el consultorio
 * @param moneda código ISO de la moneda principal
 * @param estado estado operativo del consultorio
 * @param fechaCreacion fecha de creación del registro
 * @param fechaModificacion fecha de la última modificación
 */
public record ConsultorioResponse(

        /**
         * Clave primaria interna.
         *
         * <p>Se mantiene en esta respuesta administrativa porque será útil
         * durante la construcción de relaciones internas. El frontend no
         * debe utilizarla como prueba de autorización.</p>
         */
        Long idConsultorios,

        /**
         * Identificador público del consultorio.
         *
         * <p>Este valor es más apropiado que la clave primaria para URLs,
         * referencias visibles e integraciones externas.</p>
         */
        String codigoPublico,

        /**
         * Nombre comercial o identificativo del consultorio.
         */
        String nombre,

        /**
         * RUC del consultorio.
         *
         * <p>Puede ser {@code null} si el profesional todavía no dispone de
         * esta información o no necesita registrarla.</p>
         */
        String ruc,

        /**
         * Teléfono administrativo o de contacto.
         */
        String telefono,

        /**
         * Correo administrativo.
         *
         * <p>Este correo no contiene credenciales ni representa una
         * contraseña. El acceso de usuarios se gestionará en otro módulo.</p>
         */
        String correo,

        /**
         * Dirección física o referencia del consultorio.
         */
        String direccion,

        /**
         * Zona horaria IANA utilizada para mostrar fechas y horas.
         *
         * <p>Ejemplo: {@code America/Lima}.</p>
         */
        String zonaHoraria,

        /**
         * Código ISO de tres letras de la moneda principal.
         *
         * <p>Ejemplo: {@code PEN}.</p>
         */
        String moneda,

        /**
         * Estado operativo del consultorio.
         */
        EstadoConsultorio estado,

        /**
         * Momento en el que PostgreSQL creó el registro.
         */
        OffsetDateTime fechaCreacion,

        /**
         * Momento de la última modificación registrada por PostgreSQL.
         */
        OffsetDateTime fechaModificacion

) {

    /**
     * Convierte una entidad {@link Consultorio} en una respuesta segura para
     * la API.
     *
     * <p>Este método centraliza el mapeo para evitar repetir en controladores
     * y servicios expresiones como:</p>
     *
     * <pre>
     * consultorio.getNombre();
     * consultorio.getRuc();
     * consultorio.getEstado();
     * </pre>
     *
     * <p>Si posteriormente añadimos o retiramos un campo de la respuesta,
     * podremos modificar este único lugar sin duplicar lógica.</p>
     *
     * <p>El método no modifica la entidad y no realiza consultas adicionales
     * a la base de datos.</p>
     *
     * @param consultorio entidad que se desea transformar
     * @return DTO con la información permitida para la respuesta
     * @throws NullPointerException si la entidad recibida es {@code null}
     */
    public static ConsultorioResponse desde(
            final Consultorio consultorio
    ) {
        Objects.requireNonNull(
                consultorio,
                "El consultorio es obligatorio para construir la respuesta."
        );

        return new ConsultorioResponse(
                consultorio.getIdConsultorios(),
                consultorio.getCodigoPublico(),
                consultorio.getNombre(),
                consultorio.getRuc(),
                consultorio.getTelefono(),
                consultorio.getCorreo(),
                consultorio.getDireccion(),
                consultorio.getZonaHoraria(),
                consultorio.getMoneda(),
                consultorio.getEstado(),
                consultorio.getFechaCreacion(),
                consultorio.getFechaModificacion()
        );
    }
}