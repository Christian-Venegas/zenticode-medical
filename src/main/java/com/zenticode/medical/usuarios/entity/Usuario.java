package com.zenticode.medical.usuarios.entity;

import com.zenticode.medical.consultorios.entity.Consultorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa una cuenta de usuario perteneciente a un consultorio.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    /**
     * Estados permitidos por la tabla usuarios.
     */
    public enum EstadoUsuario {
        ACTIVO,
        BLOQUEADO,
        INACTIVO
    }

    // Clave primaria generada por PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_usuarios",
            nullable = false
    )
    private Long idUsuarios;

    // Consultorio al que pertenece el usuario.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false,
            updatable = false
    )
    private Consultorio consultorio;

    // Correo utilizado para identificar la cuenta.
    @Column(
            name = "correo",
            nullable = false,
            length = 180
    )
    private String correo;

    // Hash seguro de la contraseña.
    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    // Nombres del usuario.
    @Column(
            name = "nombres",
            nullable = false,
            length = 120
    )
    private String nombres;

    // Apellidos del usuario.
    @Column(
            name = "apellidos",
            nullable = false,
            length = 120
    )
    private String apellidos;

    // Número de colegiatura profesional opcional.
    @Column(
            name = "numero_colegiatura",
            length = 40
    )
    private String numeroColegiatura;

    // Especialidad mostrada en documentos clínicos.
    @Column(
            name = "especialidad",
            length = 120
    )
    private String especialidad;

    // Ruta controlada de la firma profesional.
    @Column(
            name = "firma_url",
            length = 500
    )
    private String firmaUrl;

    // Ruta controlada del sello profesional.
    @Column(
            name = "sello_url",
            length = 500
    )
    private String selloUrl;

    // Teléfono opcional del usuario.
    @Column(
            name = "telefono",
            length = 20
    )
    private String telefono;

    // Estado actual de la cuenta.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoUsuario estado;

    // Cantidad de intentos fallidos consecutivos.
    @Column(
            name = "intentos_fallidos",
            nullable = false
    )
    private short intentosFallidos;

    // Fecha hasta la que permanece el bloqueo temporal.
    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    // Fecha del último inicio de sesión correcto.
    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    // Fecha creada automáticamente por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Fecha actualizada por el trigger de PostgreSQL.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaModificacion;

    // Constructor requerido por JPA.
    protected Usuario() {
    }

    // Crea un usuario nuevo con estado ACTIVO.
    public Usuario(
            final Consultorio consultorio,
            final String correo,
            final String passwordHash,
            final String nombres,
            final String apellidos
    ) {
        this.consultorio =
                Objects.requireNonNull(
                        consultorio,
                        "El consultorio del usuario es obligatorio."
                );

        this.correo =
                validarCorreo(correo);

        this.passwordHash =
                validarTextoObligatorio(
                        passwordHash,
                        255,
                        "El hash de la contraseña es obligatorio.",
                        "El hash de la contraseña no puede superar "
                                + "los 255 caracteres."
                );

        this.nombres =
                validarTextoObligatorio(
                        nombres,
                        120,
                        "Los nombres del usuario son obligatorios.",
                        "Los nombres del usuario no pueden superar "
                                + "los 120 caracteres."
                );

        this.apellidos =
                validarTextoObligatorio(
                        apellidos,
                        120,
                        "Los apellidos del usuario son obligatorios.",
                        "Los apellidos del usuario no pueden superar "
                                + "los 120 caracteres."
                );

        this.estado =
                EstadoUsuario.ACTIVO;

        this.intentosFallidos =
                0;

        this.numeroColegiatura =
                null;

        this.especialidad =
                null;

        this.firmaUrl =
                null;

        this.selloUrl =
                null;

        this.telefono =
                null;
    }

    // Actualiza los datos personales modificables.
    public void actualizarDatosPersonales(
            final String nombres,
            final String apellidos,
            final String numeroColegiatura,
            final String telefono
    ) {
        this.nombres =
                validarTextoObligatorio(
                        nombres,
                        120,
                        "Los nombres del usuario son obligatorios.",
                        "Los nombres del usuario no pueden superar "
                                + "los 120 caracteres."
                );

        this.apellidos =
                validarTextoObligatorio(
                        apellidos,
                        120,
                        "Los apellidos del usuario son obligatorios.",
                        "Los apellidos del usuario no pueden superar "
                                + "los 120 caracteres."
                );

        this.numeroColegiatura =
                validarTextoOpcional(
                        numeroColegiatura,
                        40,
                        "El número de colegiatura no puede superar "
                                + "los 40 caracteres."
                );

        this.telefono =
                validarTextoOpcional(
                        telefono,
                        20,
                        "El teléfono no puede superar "
                                + "los 20 caracteres."
                );
    }

    // Actualiza los datos mostrados en documentos médicos.
    public void actualizarPerfilProfesional(
            final String especialidad,
            final String firmaUrl,
            final String selloUrl
    ) {
        this.especialidad =
                validarTextoOpcionalConMinimo(
                        especialidad,
                        3,
                        120,
                        "La especialidad debe contener "
                                + "entre 3 y 120 caracteres."
                );

        this.firmaUrl =
                validarReferenciaArchivo(
                        firmaUrl,
                        "La referencia de la firma debe contener "
                                + "entre 3 y 500 caracteres."
                );

        this.selloUrl =
                validarReferenciaArchivo(
                        selloUrl,
                        "La referencia del sello debe contener "
                                + "entre 3 y 500 caracteres."
                );
    }

    // Actualiza únicamente la especialidad.
    public void actualizarEspecialidad(
            final String especialidad
    ) {
        this.especialidad =
                validarTextoOpcionalConMinimo(
                        especialidad,
                        3,
                        120,
                        "La especialidad debe contener "
                                + "entre 3 y 120 caracteres."
                );
    }

    // Actualiza únicamente la referencia de la firma.
    public void actualizarFirmaUrl(
            final String firmaUrl
    ) {
        this.firmaUrl =
                validarReferenciaArchivo(
                        firmaUrl,
                        "La referencia de la firma debe contener "
                                + "entre 3 y 500 caracteres."
                );
    }

    // Actualiza únicamente la referencia del sello.
    public void actualizarSelloUrl(
            final String selloUrl
    ) {
        this.selloUrl =
                validarReferenciaArchivo(
                        selloUrl,
                        "La referencia del sello debe contener "
                                + "entre 3 y 500 caracteres."
                );
    }

    // Elimina la referencia de la firma.
    public void eliminarFirma() {
        this.firmaUrl = null;
    }

    // Elimina la referencia del sello.
    public void eliminarSello() {
        this.selloUrl = null;
    }

    // Cambia únicamente el hash de contraseña.
    public void cambiarPasswordHash(
            final String nuevoPasswordHash
    ) {
        this.passwordHash =
                validarTextoObligatorio(
                        nuevoPasswordHash,
                        255,
                        "El nuevo hash de la contraseña "
                                + "es obligatorio.",
                        "El nuevo hash de la contraseña "
                                + "no puede superar los 255 caracteres."
                );
    }

    // Incrementa el contador sin permitir desbordamiento.
    public void registrarIntentoFallido() {
        if (intentosFallidos < Short.MAX_VALUE) {
            intentosFallidos++;
        }
    }

    // Limpia los intentos y el bloqueo temporal.
    public void restablecerIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    // Establece un bloqueo temporal futuro.
    public void bloquearTemporalmente(
            final OffsetDateTime fechaFinBloqueo
    ) {
        final OffsetDateTime fechaSegura =
                Objects.requireNonNull(
                        fechaFinBloqueo,
                        "La fecha final del bloqueo es obligatoria."
                );

        this.bloqueadoHasta =
                fechaSegura;
    }

    // Registra un acceso correcto.
    public void registrarAccesoCorrecto(
            final OffsetDateTime momentoAcceso
    ) {
        this.ultimoAcceso =
                Objects.requireNonNull(
                        momentoAcceso,
                        "La fecha del acceso es obligatoria."
                );

        restablecerIntentosFallidos();
    }

    // Bloquea administrativamente la cuenta.
    public void bloquear() {
        this.estado =
                EstadoUsuario.BLOQUEADO;
    }

    // Activa la cuenta y elimina bloqueos anteriores.
    public void activar() {
        this.estado =
                EstadoUsuario.ACTIVO;

        this.bloqueadoHasta =
                null;

        this.intentosFallidos =
                0;
    }

    // Desactiva la cuenta sin eliminarla.
    public void desactivar() {
        this.estado =
                EstadoUsuario.INACTIVO;

        this.bloqueadoHasta =
                null;
    }

    // Comprueba si el bloqueo temporal sigue vigente.
    public boolean estaBloqueadoTemporalmente(
            final OffsetDateTime momentoActual
    ) {
        Objects.requireNonNull(
                momentoActual,
                "La fecha actual es obligatoria."
        );

        return bloqueadoHasta != null
                && bloqueadoHasta.isAfter(
                momentoActual
        );
    }

    // Devuelve el nombre profesional completo.
    public String obtenerNombreCompleto() {
        return (
                nombres.trim()
                        + " "
                        + apellidos.trim()
        ).trim();
    }

    // Normaliza y valida el correo.
    private static String validarCorreo(
            final String valor
    ) {
        final String correoNormalizado =
                validarTextoObligatorio(
                        valor,
                        180,
                        "El correo del usuario es obligatorio.",
                        "El correo del usuario no puede superar "
                                + "los 180 caracteres."
                )
                        .toLowerCase(
                                Locale.ROOT
                        );

        final int primeraArroba =
                correoNormalizado.indexOf('@');

        final int ultimaArroba =
                correoNormalizado.lastIndexOf('@');

        if (primeraArroba <= 0
                || primeraArroba != ultimaArroba
                || primeraArroba
                >= correoNormalizado.length() - 1) {
            throw new IllegalArgumentException(
                    "El correo del usuario "
                            + "no tiene un formato válido."
            );
        }

        return correoNormalizado;
    }

    // Valida un texto obligatorio.
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

    // Valida un texto opcional con longitud mínima.
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

    // Valida una referencia controlada de archivo.
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

        if (contieneCaracteresDeControl(
                referencia
        )) {
            throw new IllegalArgumentException(
                    "La referencia del archivo contiene "
                            + "caracteres no permitidos."
            );
        }

        return referencia;
    }

    // Detecta caracteres de control peligrosos.
    private static boolean contieneCaracteresDeControl(
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

    public Long getIdUsuarios() {
        return idUsuarios;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public String getCorreo() {
        return correo;
    }

    // Solo debe utilizarse internamente al autenticar.
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNumeroColegiatura() {
        return numeroColegiatura;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getFirmaUrl() {
        return firmaUrl;
    }

    public String getSelloUrl() {
        return selloUrl;
    }

    public String getTelefono() {
        return telefono;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public short getIntentosFallidos() {
        return intentosFallidos;
    }

    public OffsetDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public OffsetDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    // Compara entidades usando la PK persistida.
    @Override
    public boolean equals(
            final Object objeto
    ) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Usuario usuario)) {
            return false;
        }

        return idUsuarios != null
                && Objects.equals(
                idUsuarios,
                usuario.idUsuarios
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}