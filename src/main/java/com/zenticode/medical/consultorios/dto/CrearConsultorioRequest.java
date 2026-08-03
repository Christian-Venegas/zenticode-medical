package com.zenticode.medical.consultorios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada necesarios para registrar un nuevo consultorio.
 *
 * <p>Este DTO representa el contrato que utilizará la futura API para
 * recibir información desde el frontend. No es una entidad JPA y no se
 * almacena directamente en PostgreSQL.</p>
 *
 * <p>La separación entre DTO y entidad impide que el cliente pueda enviar o
 * modificar campos internos como:</p>
 *
 * <ul>
 *     <li>{@code idConsultorios}.</li>
 *     <li>{@code codigoPublico}.</li>
 *     <li>{@code estado}.</li>
 *     <li>{@code fechaCreacion}.</li>
 *     <li>{@code fechaModificacion}.</li>
 * </ul>
 *
 * <p>El código público será generado por la capa de servicio. El estado
 * inicial será {@code ACTIVO}, y PostgreSQL controlará las fechas de creación
 * y modificación.</p>
 *
 * <p>Se utiliza un {@code record} porque este objeto solo transporta datos.
 * Los componentes de un record son inmutables después de construir la
 * instancia, lo que reduce modificaciones accidentales durante el
 * procesamiento de una petición.</p>
 *
 * @param nombre nombre comercial o identificativo del consultorio
 * @param ruc Registro Único de Contribuyentes opcional
 * @param telefono teléfono de contacto opcional
 * @param correo correo administrativo opcional
 * @param direccion dirección o referencia física opcional
 * @param zonaHoraria zona horaria IANA opcional
 * @param moneda código ISO de moneda opcional
 */
public record CrearConsultorioRequest(

        /**
         * Nombre visible del consultorio.
         *
         * <p>Es obligatorio y debe coincidir con la longitud máxima de
         * {@code consultorios.nombre} definida en PostgreSQL.</p>
         */
        @NotBlank(
                message = "El nombre del consultorio es obligatorio."
        )
        @Size(
                max = 150,
                message = "El nombre del consultorio no puede superar "
                        + "los 150 caracteres."
        )
        String nombre,

        /**
         * RUC opcional del consultorio.
         *
         * <p>Cuando se proporciona, debe contener exactamente once dígitos.
         * Una cadena vacía no se considera válida. El frontend debe enviar
         * {@code null} cuando el médico todavía no tenga RUC.</p>
         */
        @Pattern(
                regexp = "^[0-9]{11}$",
                message = "El RUC debe contener exactamente 11 dígitos."
        )
        String ruc,

        /**
         * Número telefónico de contacto.
         *
         * <p>El patrón acepta dígitos y algunos símbolos comunes de
         * presentación, pero no garantiza que el número exista. La longitud
         * máxima coincide con la columna PostgreSQL.</p>
         */
        @Size(
                max = 20,
                message = "El teléfono no puede superar los 20 caracteres."
        )
        @Pattern(
                regexp = "^[0-9+()\\-\\s]*$",
                message = "El teléfono contiene caracteres no permitidos."
        )
        String telefono,

        /**
         * Correo de contacto administrativo.
         *
         * <p>No será necesariamente el correo utilizado para iniciar sesión.
         * Las credenciales estarán asociadas posteriormente a la entidad
         * Usuario.</p>
         */
        @Email(
                message = "El correo del consultorio no tiene un formato válido."
        )
        @Size(
                max = 180,
                message = "El correo no puede superar los 180 caracteres."
        )
        String correo,

        /**
         * Dirección física o referencia del consultorio.
         */
        @Size(
                max = 250,
                message = "La dirección no puede superar los 250 caracteres."
        )
        String direccion,

        /**
         * Zona horaria utilizada por el consultorio.
         *
         * <p>El valor debe utilizar el formato de identificadores IANA, por
         * ejemplo {@code America/Lima}. Si no se proporciona, la capa de
         * servicio utilizará {@code America/Lima} como valor inicial.</p>
         *
         * <p>La existencia real del identificador se comprobará en la capa de
         * servicio mediante {@code ZoneId.of(...)}, ya que una expresión
         * regular no puede validar correctamente el catálogo de zonas
         * horarias disponible en Java.</p>
         */
        @Size(
                max = 60,
                message = "La zona horaria no puede superar los "
                        + "60 caracteres."
        )
        String zonaHoraria,

        /**
         * Código ISO de tres letras de la moneda.
         *
         * <p>Ejemplos: {@code PEN}, {@code USD} y {@code EUR}. Si el cliente
         * no envía un valor, la capa de servicio utilizará {@code PEN}.</p>
         *
         * <p>La expresión acepta mayúsculas y minúsculas porque la entidad
         * normalizará posteriormente la moneda a mayúsculas.</p>
         */
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "La moneda debe contener exactamente tres letras."
        )
        String moneda

) {

    /**
     * Constructor compacto utilizado para normalizar las cadenas antes de
     * almacenar los componentes del record.
     *
     * <p>Las validaciones de Jakarta se ejecutan cuando un controlador
     * recibe el DTO con {@code @Valid}. Este constructor no sustituye esas
     * validaciones. Su responsabilidad es normalizar la representación de
     * los valores.</p>
     *
     * <p>Las cadenas opcionales vacías se convierten en {@code null}. Así se
     * evita representar la ausencia de información de dos maneras
     * diferentes:</p>
     *
     * <ul>
     *     <li>Cadena vacía.</li>
     *     <li>Valor nulo.</li>
     * </ul>
     */
    public CrearConsultorioRequest {
        nombre = normalizarTexto(nombre);
        ruc = normalizarOpcional(ruc);
        telefono = normalizarOpcional(telefono);
        correo = normalizarCorreo(correo);
        direccion = normalizarOpcional(direccion);
        zonaHoraria = normalizarOpcional(zonaHoraria);
        moneda = normalizarMoneda(moneda);
    }

    /**
     * Elimina espacios laterales de un valor requerido.
     *
     * <p>Si el valor llega como {@code null}, permanece nulo para que
     * {@link NotBlank} produzca el mensaje de validación correspondiente.</p>
     *
     * @param valor texto recibido
     * @return texto sin espacios laterales o {@code null}
     */
    private static String normalizarTexto(final String valor) {
        if (valor == null) {
            return null;
        }

        return valor.trim();
    }

    /**
     * Normaliza un valor opcional.
     *
     * @param valor texto recibido
     * @return texto sin espacios, o {@code null} cuando está ausente o vacío
     */
    private static String normalizarOpcional(final String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    /**
     * Normaliza un correo electrónico opcional.
     *
     * <p>Los correos se almacenarán en minúsculas para evitar diferencias
     * artificiales entre valores como {@code MEDICO@CORREO.COM} y
     * {@code medico@correo.com}.</p>
     *
     * @param correo correo recibido
     * @return correo normalizado o {@code null}
     */
    private static String normalizarCorreo(final String correo) {
        final String correoNormalizado = normalizarOpcional(correo);

        if (correoNormalizado == null) {
            return null;
        }

        return correoNormalizado.toLowerCase();
    }

    /**
     * Normaliza el código de moneda opcional.
     *
     * @param moneda moneda recibida
     * @return moneda en mayúsculas o {@code null}
     */
    private static String normalizarMoneda(final String moneda) {
        final String monedaNormalizada = normalizarOpcional(moneda);

        if (monedaNormalizada == null) {
            return null;
        }

        return monedaNormalizada.toUpperCase();
    }
}