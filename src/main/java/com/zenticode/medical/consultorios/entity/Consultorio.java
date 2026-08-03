package com.zenticode.medical.consultorios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa un consultorio registrado en Zenticode Medical.
 */
@Entity
@Table(name = "consultorios")
public class Consultorio {

    /**
     * Estados operativos permitidos.
     */
    public enum EstadoConsultorio {
        ACTIVO,
        SUSPENDIDO,
        INACTIVO
    }

    // Clave primaria interna.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_consultorios",
            nullable = false
    )
    private Long idConsultorios;

    // Identificador público no secuencial.
    @Column(
            name = "codigo_publico",
            nullable = false,
            unique = true,
            length = 36
    )
    private String codigoPublico;

    // Nombre comercial o identificativo.
    @Column(
            name = "nombre",
            nullable = false,
            length = 150
    )
    private String nombre;

    // RUC opcional del consultorio.
    @Column(
            name = "ruc",
            length = 11
    )
    private String ruc;

    // Número telefónico principal.
    @Column(
            name = "telefono",
            length = 20
    )
    private String telefono;

    // Correo administrativo.
    @Column(
            name = "correo",
            length = 180
    )
    private String correo;

    // Dirección física o referencia.
    @Column(
            name = "direccion",
            length = 250
    )
    private String direccion;

    // Subtítulo mostrado en documentos clínicos.
    @Column(
            name = "descripcion_documentos",
            length = 200
    )
    private String descripcionDocumentos;

    // Ruta controlada del logo del consultorio.
    @Column(
            name = "logo_url",
            length = 500
    )
    private String logoUrl;

    // Zona horaria aplicada al consultorio.
    @Column(
            name = "zona_horaria",
            nullable = false,
            length = 60
    )
    private String zonaHoraria;

    // Moneda principal en formato ISO de tres caracteres.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "moneda",
            nullable = false,
            length = 3
    )
    private String moneda;

    // Estado operativo actual.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoConsultorio estado;

    // Fecha administrada por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Fecha actualizada mediante trigger.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaModificacion;

    // Constructor requerido por JPA.
    protected Consultorio() {
    }

    // Crea un consultorio con sus datos mínimos.
    public Consultorio(
            final String codigoPublico,
            final String nombre
    ) {
        this.codigoPublico =
                validarTextoObligatorio(
                        codigoPublico,
                        36,
                        "El código público es obligatorio.",
                        "El código público no puede superar "
                                + "los 36 caracteres."
                );

        this.nombre =
                validarTextoObligatorio(
                        nombre,
                        150,
                        "El nombre del consultorio es obligatorio.",
                        "El nombre del consultorio no puede superar "
                                + "los 150 caracteres."
                );

        this.zonaHoraria =
                "America/Lima";

        this.moneda =
                "PEN";

        this.estado =
                EstadoConsultorio.ACTIVO;

        this.descripcionDocumentos =
                null;

        this.logoUrl =
                null;
    }

    // Actualiza los datos administrativos.
    public void actualizarDatos(
            final String nombre,
            final String ruc,
            final String telefono,
            final String correo,
            final String direccion,
            final String zonaHoraria,
            final String moneda
    ) {
        this.nombre =
                validarTextoObligatorio(
                        nombre,
                        150,
                        "El nombre del consultorio es obligatorio.",
                        "El nombre del consultorio no puede superar "
                                + "los 150 caracteres."
                );

        this.ruc =
                validarRuc(ruc);

        this.telefono =
                validarTextoOpcional(
                        telefono,
                        20,
                        "El teléfono no puede superar "
                                + "los 20 caracteres."
                );

        this.correo =
                validarCorreo(correo);

        this.direccion =
                validarTextoOpcional(
                        direccion,
                        250,
                        "La dirección no puede superar "
                                + "los 250 caracteres."
                );

        this.zonaHoraria =
                validarTextoObligatorio(
                        zonaHoraria,
                        60,
                        "La zona horaria es obligatoria.",
                        "La zona horaria no puede superar "
                                + "los 60 caracteres."
                );

        this.moneda =
                validarMoneda(moneda);
    }

    // Actualiza la identidad visual de documentos clínicos.
    public void actualizarPersonalizacionDocumentos(
            final String descripcionDocumentos,
            final String logoUrl
    ) {
        this.descripcionDocumentos =
                validarTextoOpcionalConMinimo(
                        descripcionDocumentos,
                        3,
                        200,
                        "La descripción de documentos debe contener "
                                + "entre 3 y 200 caracteres."
                );

        this.logoUrl =
                validarReferenciaArchivo(
                        logoUrl,
                        "La referencia del logo debe contener "
                                + "entre 3 y 500 caracteres."
                );
    }

    // Actualiza únicamente el subtítulo documental.
    public void actualizarDescripcionDocumentos(
            final String descripcionDocumentos
    ) {
        this.descripcionDocumentos =
                validarTextoOpcionalConMinimo(
                        descripcionDocumentos,
                        3,
                        200,
                        "La descripción de documentos debe contener "
                                + "entre 3 y 200 caracteres."
                );
    }

    // Actualiza únicamente la referencia del logo.
    public void actualizarLogoUrl(
            final String logoUrl
    ) {
        this.logoUrl =
                validarReferenciaArchivo(
                        logoUrl,
                        "La referencia del logo debe contener "
                                + "entre 3 y 500 caracteres."
                );
    }

    // Retira la referencia del logo.
    public void eliminarLogo() {
        this.logoUrl = null;
    }

    // Suspende temporalmente el consultorio.
    public void suspender() {
        if (estado == EstadoConsultorio.INACTIVO) {
            throw new IllegalStateException(
                    "Un consultorio inactivo no puede suspenderse."
            );
        }

        this.estado =
                EstadoConsultorio.SUSPENDIDO;
    }

    // Reactiva el consultorio.
    public void activar() {
        this.estado =
                EstadoConsultorio.ACTIVO;
    }

    // Desactiva el consultorio sin eliminarlo.
    public void desactivar() {
        this.estado =
                EstadoConsultorio.INACTIVO;
    }

    // Valida un texto obligatorio y su longitud máxima.
    private static String validarTextoObligatorio(
            final String valor,
            final int longitudMaxima,
            final String mensajeObligatorio,
            final String mensajeLongitud
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    mensajeObligatorio
            );
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    mensajeLongitud
            );
        }

        return texto;
    }

    // Normaliza y valida un texto opcional.
    private static String validarTextoOpcional(
            final String valor,
            final int longitudMaxima,
            final String mensajeLongitud
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    mensajeLongitud
            );
        }

        return texto;
    }

    // Valida una configuración opcional con longitud mínima.
    private static String validarTextoOpcionalConMinimo(
            final String valor,
            final int longitudMinima,
            final int longitudMaxima,
            final String mensajeLongitud
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String texto =
                valor.trim();

        if (texto.length() < longitudMinima
                || texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    mensajeLongitud
            );
        }

        return texto;
    }

    // Valida la referencia controlada de un archivo.
    private static String validarReferenciaArchivo(
            final String valor,
            final String mensajeLongitud
    ) {
        final String referencia =
                validarTextoOpcionalConMinimo(
                        valor,
                        3,
                        500,
                        mensajeLongitud
                );

        if (referencia == null) {
            return null;
        }

        if (contieneCaracteresPeligrosos(
                referencia
        )) {
            throw new IllegalArgumentException(
                    "La referencia del archivo contiene "
                            + "caracteres no permitidos."
            );
        }

        return referencia;
    }

    // Evita referencias con caracteres de control.
    private static boolean contieneCaracteresPeligrosos(
            final String valor
    ) {
        for (int indice = 0;
             indice < valor.length();
             indice++) {

            if (Character.isISOControl(
                    valor.charAt(indice)
            )) {
                return true;
            }
        }

        return false;
    }

    // Valida el RUC opcional.
    private static String validarRuc(
            final String valor
    ) {
        final String rucNormalizado =
                validarTextoOpcional(
                        valor,
                        11,
                        "El RUC no puede superar "
                                + "los 11 caracteres."
                );

        if (rucNormalizado == null) {
            return null;
        }

        if (!rucNormalizado.matches(
                "^[0-9]{11}$"
        )) {
            throw new IllegalArgumentException(
                    "El RUC debe contener exactamente "
                            + "11 dígitos."
            );
        }

        return rucNormalizado;
    }

    // Normaliza y valida el correo administrativo.
    private static String validarCorreo(
            final String valor
    ) {
        final String correoNormalizado =
                validarTextoOpcional(
                        valor,
                        180,
                        "El correo no puede superar "
                                + "los 180 caracteres."
                );

        if (correoNormalizado == null) {
            return null;
        }

        final String correoSeguro =
                correoNormalizado.toLowerCase(
                        Locale.ROOT
                );

        final int posicionArroba =
                correoSeguro.indexOf('@');

        final int ultimaPosicionArroba =
                correoSeguro.lastIndexOf('@');

        if (posicionArroba <= 0
                || posicionArroba != ultimaPosicionArroba
                || posicionArroba
                >= correoSeguro.length() - 1) {
            throw new IllegalArgumentException(
                    "El correo del consultorio "
                            + "no tiene un formato válido."
            );
        }

        return correoSeguro;
    }

    // Valida el código ISO de moneda.
    private static String validarMoneda(
            final String moneda
    ) {
        final String monedaNormalizada =
                validarTextoObligatorio(
                        moneda,
                        3,
                        "La moneda es obligatoria.",
                        "La moneda debe contener exactamente "
                                + "tres caracteres."
                )
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!monedaNormalizada.matches(
                "^[A-Z]{3}$"
        )) {
            throw new IllegalArgumentException(
                    "La moneda debe contener exactamente "
                            + "tres letras."
            );
        }

        return monedaNormalizada;
    }

    public Long getIdConsultorios() {
        return idConsultorios;
    }

    public String getCodigoPublico() {
        return codigoPublico;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getDescripcionDocumentos() {
        return descripcionDocumentos;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public String getMoneda() {
        return moneda;
    }

    public EstadoConsultorio getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    // Compara consultorios mediante su PK persistida.
    @Override
    public boolean equals(
            final Object objeto
    ) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto
                instanceof Consultorio consultorio)) {
            return false;
        }

        return idConsultorios != null
                && Objects.equals(
                idConsultorios,
                consultorio.idConsultorios
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}