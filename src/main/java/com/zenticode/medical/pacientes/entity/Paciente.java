package com.zenticode.medical.pacientes.entity;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa los datos administrativos de un paciente.
 */
@Entity
@Table(name = "pacientes")
public class Paciente {

    // Clave primaria generada por PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_pacientes",
            nullable = false
    )
    private Long idPacientes;

    // Consultorio propietario de la información.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false
    )
    private Consultorio consultorio;

    // Tipo de documento del paciente.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_documento",
            nullable = false,
            length = 20
    )
    private TipoDocumento tipoDocumento;

    // Documento único dentro del consultorio.
    @Column(
            name = "numero_documento",
            nullable = false,
            length = 20
    )
    private String numeroDocumento;

    // Nombres del paciente.
    @Column(
            name = "nombres",
            nullable = false,
            length = 100
    )
    private String nombres;

    // Apellidos del paciente.
    @Column(
            name = "apellidos",
            nullable = false,
            length = 100
    )
    private String apellidos;

    // Fecha de nacimiento opcional.
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // Teléfono principal.
    @Column(
            name = "telefono",
            length = 20
    )
    private String telefono;

    // Correo opcional del paciente.
    @Column(
            name = "correo",
            length = 180
    )
    private String correo;

    // Dirección de contacto.
    @Column(
            name = "direccion",
            length = 250
    )
    private String direccion;

    // Persona de contacto para emergencias.
    @Column(
            name = "contacto_emergencia",
            length = 150
    )
    private String contactoEmergencia;

    // Teléfono del contacto de emergencia.
    @Column(
            name = "telefono_emergencia",
            length = 20
    )
    private String telefonoEmergencia;

    // Estado lógico del paciente.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoPaciente estado;

    // Fecha administrada inicialmente por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Fecha actualizada por la aplicación.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime fechaModificacion;

    // Constructor requerido por JPA.
    protected Paciente() {
    }

    // Crea un paciente activo dentro de un consultorio.
    public Paciente(
            final Consultorio consultorio,
            final TipoDocumento tipoDocumento,
            final String numeroDocumento,
            final String nombres,
            final String apellidos,
            final LocalDate fechaNacimiento,
            final String telefono,
            final String correo,
            final String direccion,
            final String contactoEmergencia,
            final String telefonoEmergencia
    ) {
        this.consultorio = Objects.requireNonNull(
                consultorio,
                "El consultorio del paciente es obligatorio."
        );

        this.tipoDocumento = Objects.requireNonNull(
                tipoDocumento,
                "El tipo de documento es obligatorio."
        );

        this.numeroDocumento =
                normalizarDocumento(numeroDocumento);

        this.nombres = validarTextoObligatorio(
                nombres,
                100,
                "Los nombres del paciente son obligatorios."
        );

        this.apellidos = validarTextoObligatorio(
                apellidos,
                100,
                "Los apellidos del paciente son obligatorios."
        );

        this.fechaNacimiento =
                validarFechaNacimiento(fechaNacimiento);

        this.telefono =
                normalizarTextoOpcional(telefono, 20);

        this.correo =
                normalizarCorreo(correo);

        this.direccion =
                normalizarTextoOpcional(direccion, 250);

        this.contactoEmergencia =
                normalizarTextoOpcional(
                        contactoEmergencia,
                        150
                );

        this.telefonoEmergencia =
                normalizarTextoOpcional(
                        telefonoEmergencia,
                        20
                );

        this.estado = EstadoPaciente.ACTIVO;
    }

    // Actualiza los datos administrativos del paciente.
    public void actualizar(
            final TipoDocumento tipoDocumento,
            final String numeroDocumento,
            final String nombres,
            final String apellidos,
            final LocalDate fechaNacimiento,
            final String telefono,
            final String correo,
            final String direccion,
            final String contactoEmergencia,
            final String telefonoEmergencia
    ) {
        this.tipoDocumento = Objects.requireNonNull(
                tipoDocumento,
                "El tipo de documento es obligatorio."
        );

        this.numeroDocumento =
                normalizarDocumento(numeroDocumento);

        this.nombres = validarTextoObligatorio(
                nombres,
                100,
                "Los nombres del paciente son obligatorios."
        );

        this.apellidos = validarTextoObligatorio(
                apellidos,
                100,
                "Los apellidos del paciente son obligatorios."
        );

        this.fechaNacimiento =
                validarFechaNacimiento(fechaNacimiento);

        this.telefono =
                normalizarTextoOpcional(telefono, 20);

        this.correo =
                normalizarCorreo(correo);

        this.direccion =
                normalizarTextoOpcional(direccion, 250);

        this.contactoEmergencia =
                normalizarTextoOpcional(
                        contactoEmergencia,
                        150
                );

        this.telefonoEmergencia =
                normalizarTextoOpcional(
                        telefonoEmergencia,
                        20
                );

        this.fechaModificacion =
                OffsetDateTime.now();
    }

    // Desactiva al paciente sin eliminar su información.
    public void desactivar() {
        this.estado = EstadoPaciente.INACTIVO;
        this.fechaModificacion = OffsetDateTime.now();
    }

    // Reactiva un paciente desactivado.
    public void activar() {
        this.estado = EstadoPaciente.ACTIVO;
        this.fechaModificacion = OffsetDateTime.now();
    }

    // Valida y normaliza el documento.
    private static String normalizarDocumento(
            final String valor
    ) {
        final String documento =
                validarTextoObligatorio(
                        valor,
                        20,
                        "El número de documento es obligatorio."
                )
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "");

        if (documento.length() < 4) {
            throw new IllegalArgumentException(
                    "El número de documento debe contener "
                            + "al menos 4 caracteres."
            );
        }

        if (!documento.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException(
                    "El número de documento contiene "
                            + "caracteres no permitidos."
            );
        }

        return documento;
    }

    // Comprueba que la fecha no esté en el futuro.
    private static LocalDate validarFechaNacimiento(
            final LocalDate fecha
    ) {
        if (fecha != null
                && fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento "
                            + "no puede estar en el futuro."
            );
        }

        return fecha;
    }

    // Normaliza un correo opcional.
    private static String normalizarCorreo(
            final String valor
    ) {
        final String correoNormalizado =
                normalizarTextoOpcional(valor, 180);

        if (correoNormalizado == null) {
            return null;
        }

        final String correo =
                correoNormalizado.toLowerCase(Locale.ROOT);

        if (!correo.matches(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$"
                        .toLowerCase(Locale.ROOT)
        )) {
            throw new IllegalArgumentException(
                    "El correo del paciente "
                            + "no tiene un formato válido."
            );
        }

        return correo;
    }

    // Valida y limpia un texto obligatorio.
    private static String validarTextoObligatorio(
            final String valor,
            final int longitudMaxima,
            final String mensaje
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }

        final String texto =
                valor.trim();

        if (texto.length() < 2) {
            throw new IllegalArgumentException(mensaje);
        }

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    "El valor no puede superar los "
                            + longitudMaxima
                            + " caracteres."
            );
        }

        return texto;
    }

    // Convierte textos opcionales vacíos en null.
    private static String normalizarTextoOpcional(
            final String valor,
            final int longitudMaxima
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    "El valor no puede superar los "
                            + longitudMaxima
                            + " caracteres."
            );
        }

        return texto;
    }

    public Long getIdPacientes() {
        return idPacientes;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
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

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public EstadoPaciente getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    // Compara pacientes mediante su PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Paciente paciente)) {
            return false;
        }

        return idPacientes != null
                && Objects.equals(
                idPacientes,
                paciente.idPacientes
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Tipos de documento permitidos.
     */
    public enum TipoDocumento {
        DNI,
        CARNET_EXTRANJERIA,
        PASAPORTE,
        OTRO
    }

    /**
     * Estados lógicos del paciente.
     */
    public enum EstadoPaciente {
        ACTIVO,
        INACTIVO
    }
}