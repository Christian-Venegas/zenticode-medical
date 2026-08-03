package com.zenticode.medical.historias.entity;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.pacientes.entity.Paciente;
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
import java.util.UUID;

/**
 * Representa la historia clínica general de un paciente.
 */
@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica {

    // Clave primaria de la historia clínica.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_historias_clinicas",
            nullable = false
    )
    private Long idHistoriasClinicas;

    // Consultorio propietario de la historia clínica.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false
    )
    private Consultorio consultorio;

    // Paciente propietario de la historia clínica.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_pacientes",
            nullable = false
    )
    private Paciente paciente;

    // Número único de historia dentro del consultorio.
    @Column(
            name = "numero_historia",
            nullable = false,
            length = 30
    )
    private String numeroHistoria;

    // Grupo sanguíneo declarado.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "grupo_sanguineo",
            nullable = false,
            length = 20
    )
    private GrupoSanguineo grupoSanguineo;

    // Ocupación informada por el paciente.
    @Column(
            name = "ocupacion",
            length = 120
    )
    private String ocupacion;

    // Estado civil informado.
    @Column(
            name = "estado_civil",
            length = 50
    )
    private String estadoCivil;

    // Lugar de nacimiento informado.
    @Column(
            name = "lugar_nacimiento",
            length = 150
    )
    private String lugarNacimiento;

    // Antecedentes propios del paciente.
    @Column(
            name = "antecedentes_personales",
            columnDefinition = "TEXT"
    )
    private String antecedentesPersonales;

    // Antecedentes médicos familiares.
    @Column(
            name = "antecedentes_familiares",
            columnDefinition = "TEXT"
    )
    private String antecedentesFamiliares;

    // Cirugías y procedimientos anteriores.
    @Column(
            name = "antecedentes_quirurgicos",
            columnDefinition = "TEXT"
    )
    private String antecedentesQuirurgicos;

    // Medicamentos y antecedentes farmacológicos.
    @Column(
            name = "antecedentes_farmacologicos",
            columnDefinition = "TEXT"
    )
    private String antecedentesFarmacologicos;

    // Información clínica general adicional.
    @Column(
            name = "observaciones_generales",
            length = 2000
    )
    private String observacionesGenerales;

    // Estado lógico de la historia clínica.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoHistoriaClinica estado;

    // Fecha de apertura administrada por PostgreSQL.
    @Column(
            name = "fecha_apertura",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaApertura;

    // Última modificación de la historia.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime fechaModificacion;

    // Usuario que abrió la historia clínica.
    @Column(
            name = "creado_por",
            nullable = false,
            updatable = false
    )
    private Long creadoPor;

    // Usuario responsable de la última modificación.
    @Column(
            name = "modificado_por",
            nullable = false
    )
    private Long modificadoPor;

    // Constructor requerido por JPA.
    protected HistoriaClinica() {
    }

    // Mantiene compatibilidad con el servicio actual.
    public HistoriaClinica(
            final Consultorio consultorio,
            final Paciente paciente,
            final Long idUsuarioResponsable,
            final String grupoSanguineo,
            final String antecedentesPersonales,
            final String antecedentesFamiliares,
            final String antecedentesQuirurgicos,
            final String antecedentesFarmacologicos,
            final String observacionesGenerales
    ) {
        this(
                consultorio,
                paciente,
                generarNumeroHistoria(),
                idUsuarioResponsable,
                grupoSanguineo,
                null,
                null,
                null,
                antecedentesPersonales,
                antecedentesFamiliares,
                antecedentesQuirurgicos,
                antecedentesFarmacologicos,
                observacionesGenerales
        );
    }

    // Abre una historia con todos los campos de V9.
    public HistoriaClinica(
            final Consultorio consultorio,
            final Paciente paciente,
            final String numeroHistoria,
            final Long idUsuarioResponsable,
            final String grupoSanguineo,
            final String ocupacion,
            final String estadoCivil,
            final String lugarNacimiento,
            final String antecedentesPersonales,
            final String antecedentesFamiliares,
            final String antecedentesQuirurgicos,
            final String antecedentesFarmacologicos,
            final String observacionesGenerales
    ) {
        this.consultorio =
                Objects.requireNonNull(
                        consultorio,
                        "El consultorio es obligatorio."
                );

        this.paciente =
                Objects.requireNonNull(
                        paciente,
                        "El paciente es obligatorio."
                );

        validarMismoConsultorio(
                consultorio,
                paciente
        );

        validarIdUsuario(
                idUsuarioResponsable
        );

        this.numeroHistoria =
                normalizarNumeroHistoria(
                        numeroHistoria
                );

        this.grupoSanguineo =
                normalizarGrupoSanguineo(
                        grupoSanguineo
                );

        this.ocupacion =
                normalizarTextoOpcional(
                        ocupacion,
                        120
                );

        this.estadoCivil =
                normalizarTextoOpcional(
                        estadoCivil,
                        50
                );

        this.lugarNacimiento =
                normalizarTextoOpcional(
                        lugarNacimiento,
                        150
                );

        this.antecedentesPersonales =
                normalizarTextoClinico(
                        antecedentesPersonales
                );

        this.antecedentesFamiliares =
                normalizarTextoClinico(
                        antecedentesFamiliares
                );

        this.antecedentesQuirurgicos =
                normalizarTextoClinico(
                        antecedentesQuirurgicos
                );

        this.antecedentesFarmacologicos =
                normalizarTextoClinico(
                        antecedentesFarmacologicos
                );

        this.observacionesGenerales =
                normalizarTextoOpcional(
                        observacionesGenerales,
                        2000
                );

        this.estado =
                EstadoHistoriaClinica.ACTIVA;

        this.creadoPor =
                idUsuarioResponsable;

        this.modificadoPor =
                idUsuarioResponsable;
    }

    // Mantiene compatibilidad con la actualización actual.
    public void actualizar(
            final Long idUsuarioResponsable,
            final String grupoSanguineo,
            final String antecedentesPersonales,
            final String antecedentesFamiliares,
            final String antecedentesQuirurgicos,
            final String antecedentesFarmacologicos,
            final String observacionesGenerales
    ) {
        actualizar(
                idUsuarioResponsable,
                grupoSanguineo,
                ocupacion,
                estadoCivil,
                lugarNacimiento,
                antecedentesPersonales,
                antecedentesFamiliares,
                antecedentesQuirurgicos,
                antecedentesFarmacologicos,
                observacionesGenerales
        );
    }

    // Actualiza todos los campos disponibles en V9.
    public void actualizar(
            final Long idUsuarioResponsable,
            final String grupoSanguineo,
            final String ocupacion,
            final String estadoCivil,
            final String lugarNacimiento,
            final String antecedentesPersonales,
            final String antecedentesFamiliares,
            final String antecedentesQuirurgicos,
            final String antecedentesFarmacologicos,
            final String observacionesGenerales
    ) {
        validarIdUsuario(
                idUsuarioResponsable
        );

        verificarHistoriaActiva();

        this.grupoSanguineo =
                normalizarGrupoSanguineo(
                        grupoSanguineo
                );

        this.ocupacion =
                normalizarTextoOpcional(
                        ocupacion,
                        120
                );

        this.estadoCivil =
                normalizarTextoOpcional(
                        estadoCivil,
                        50
                );

        this.lugarNacimiento =
                normalizarTextoOpcional(
                        lugarNacimiento,
                        150
                );

        this.antecedentesPersonales =
                normalizarTextoClinico(
                        antecedentesPersonales
                );

        this.antecedentesFamiliares =
                normalizarTextoClinico(
                        antecedentesFamiliares
                );

        this.antecedentesQuirurgicos =
                normalizarTextoClinico(
                        antecedentesQuirurgicos
                );

        this.antecedentesFarmacologicos =
                normalizarTextoClinico(
                        antecedentesFarmacologicos
                );

        this.observacionesGenerales =
                normalizarTextoOpcional(
                        observacionesGenerales,
                        2000
                );

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Cierra la historia sin eliminar información médica.
    public void archivar(
            final Long idUsuarioResponsable
    ) {
        validarIdUsuario(
                idUsuarioResponsable
        );

        if (
                estado
                        == EstadoHistoriaClinica.CERRADA
        ) {
            return;
        }

        this.estado =
                EstadoHistoriaClinica.CERRADA;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Alias explícito para el nuevo contrato.
    public void cerrar(
            final Long idUsuarioResponsable
    ) {
        archivar(
                idUsuarioResponsable
        );
    }

    // Reactiva una historia cerrada.
    public void reabrir(
            final Long idUsuarioResponsable
    ) {
        validarIdUsuario(
                idUsuarioResponsable
        );

        if (
                estado
                        == EstadoHistoriaClinica.ACTIVA
        ) {
            return;
        }

        this.estado =
                EstadoHistoriaClinica.ACTIVA;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Indica si la historia admite modificaciones.
    public boolean estaActiva() {
        return estado
                == EstadoHistoriaClinica.ACTIVA;
    }

    // Comprueba que la historia continúe activa.
    private void verificarHistoriaActiva() {
        if (!estaActiva()) {
            throw new IllegalStateException(
                    "La historia clínica no se encuentra activa."
            );
        }
    }

    // Registra la auditoría de modificación.
    private void registrarModificacion(
            final Long idUsuarioResponsable
    ) {
        validarIdUsuario(
                idUsuarioResponsable
        );

        this.modificadoPor =
                idUsuarioResponsable;

        this.fechaModificacion =
                OffsetDateTime.now();
    }

    // Comprueba que paciente e historia sean del mismo consultorio.
    private static void validarMismoConsultorio(
            final Consultorio consultorio,
            final Paciente paciente
    ) {
        final Long idConsultorios =
                consultorio.getIdConsultorios();

        final Long idConsultoriosPaciente =
                paciente
                        .getConsultorio()
                        .getIdConsultorios();

        if (
                idConsultorios != null
                        && idConsultoriosPaciente != null
                        && !Objects.equals(
                        idConsultorios,
                        idConsultoriosPaciente
                )
        ) {
            throw new IllegalArgumentException(
                    "El paciente no pertenece al consultorio."
            );
        }

        // Valida entidades nuevas aún no persistidas.
        if (
                (
                        idConsultorios == null
                                || idConsultoriosPaciente == null
                )
                        && paciente.getConsultorio()
                        != consultorio
        ) {
            throw new IllegalArgumentException(
                    "El paciente no pertenece al consultorio."
            );
        }
    }

    // Valida el identificador del responsable.
    private static void validarIdUsuario(
            final Long idUsuarioResponsable
    ) {
        if (
                idUsuarioResponsable == null
                        || idUsuarioResponsable <= 0
        ) {
            throw new IllegalArgumentException(
                    "El profesional responsable es obligatorio."
            );
        }
    }

    // Genera un número único para una nueva historia.
    private static String generarNumeroHistoria() {
        final String fragmento =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase(Locale.ROOT);

        return "HC-" + fragmento;
    }

    // Normaliza el número de historia.
    private static String normalizarNumeroHistoria(
            final String valor
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El número de historia es obligatorio."
            );
        }

        final String numero =
                valor
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "");

        if (
                numero.length() > 30
        ) {
            throw new IllegalArgumentException(
                    "El número de historia no puede "
                            + "superar los 30 caracteres."
            );
        }

        if (
                !numero.matches(
                        "^[A-Z0-9-]+$"
                )
        ) {
            throw new IllegalArgumentException(
                    "El número de historia contiene "
                            + "caracteres no permitidos."
            );
        }

        return numero;
    }

    // Convierte formatos visibles al enum persistido.
    private static GrupoSanguineo normalizarGrupoSanguineo(
            final String valor
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            return GrupoSanguineo.DESCONOCIDO;
        }

        final String grupo =
                valor
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "_");

        return switch (grupo) {
            case "A+", "A_POSITIVO" ->
                    GrupoSanguineo.A_POSITIVO;

            case "A-", "A_NEGATIVO" ->
                    GrupoSanguineo.A_NEGATIVO;

            case "B+", "B_POSITIVO" ->
                    GrupoSanguineo.B_POSITIVO;

            case "B-", "B_NEGATIVO" ->
                    GrupoSanguineo.B_NEGATIVO;

            case "AB+", "AB_POSITIVO" ->
                    GrupoSanguineo.AB_POSITIVO;

            case "AB-", "AB_NEGATIVO" ->
                    GrupoSanguineo.AB_NEGATIVO;

            case "O+", "O_POSITIVO" ->
                    GrupoSanguineo.O_POSITIVO;

            case "O-", "O_NEGATIVO" ->
                    GrupoSanguineo.O_NEGATIVO;

            case "DESCONOCIDO" ->
                    GrupoSanguineo.DESCONOCIDO;

            default ->
                    throw new IllegalArgumentException(
                            "El grupo sanguíneo no tiene "
                                    + "un formato válido."
                    );
        };
    }

    // Normaliza textos opcionales con límite concreto.
    private static String normalizarTextoOpcional(
            final String valor,
            final int longitudMaxima
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            return null;
        }

        final String texto =
                valor.trim();

        if (
                texto.length() > longitudMaxima
        ) {
            throw new IllegalArgumentException(
                    "El valor no puede superar los "
                            + longitudMaxima
                            + " caracteres."
            );
        }

        return texto;
    }

    // Conserva temporalmente textos clínicos antiguos.
    private static String normalizarTextoClinico(
            final String valor
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            return null;
        }

        final String texto =
                valor.trim();

        if (
                texto.length() > 10000
        ) {
            throw new IllegalArgumentException(
                    "El texto clínico no puede superar "
                            + "los 10000 caracteres."
            );
        }

        return texto;
    }

    public Long getIdHistoriasClinicas() {
        return idHistoriasClinicas;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public String getNumeroHistoria() {
        return numeroHistoria;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo.name();
    }

    public GrupoSanguineo getGrupoSanguineoEnum() {
        return grupoSanguineo;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public String getLugarNacimiento() {
        return lugarNacimiento;
    }

    public String getAntecedentesPersonales() {
        return antecedentesPersonales;
    }

    public String getAntecedentesFamiliares() {
        return antecedentesFamiliares;
    }

    public String getAntecedentesQuirurgicos() {
        return antecedentesQuirurgicos;
    }

    public String getAntecedentesFarmacologicos() {
        return antecedentesFarmacologicos;
    }

    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public EstadoHistoriaClinica getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaApertura() {
        return fechaApertura;
    }

    public OffsetDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Long getCreadoPor() {
        return creadoPor;
    }

    public Long getModificadoPor() {
        return modificadoPor;
    }

    // Compara historias mediante su PK persistida.
    @Override
    public boolean equals(
            final Object objeto
    ) {
        if (this == objeto) {
            return true;
        }

        if (
                !(objeto instanceof
                        HistoriaClinica historia)
        ) {
            return false;
        }

        return idHistoriasClinicas != null
                && Objects.equals(
                idHistoriasClinicas,
                historia.idHistoriasClinicas
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Grupos sanguíneos persistidos.
     */
    public enum GrupoSanguineo {
        A_POSITIVO,
        A_NEGATIVO,
        B_POSITIVO,
        B_NEGATIVO,
        AB_POSITIVO,
        AB_NEGATIVO,
        O_POSITIVO,
        O_NEGATIVO,
        DESCONOCIDO
    }

    /**
     * Estados lógicos permitidos.
     */
    public enum EstadoHistoriaClinica {
        ACTIVA,
        CERRADA
    }
}