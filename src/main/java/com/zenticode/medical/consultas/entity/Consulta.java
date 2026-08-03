package com.zenticode.medical.consultas.entity;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.historias.entity.HistoriaClinica;
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
import java.util.Objects;

/**
 * Representa una atención médica dentro del historial clínico.
 */
@Entity
@Table(name = "consultas")
public class Consulta {

    private static final int LONGITUD_MAXIMA_TEXTO_CLINICO =
            20000;

    // Clave primaria de la consulta médica.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_consultas",
            nullable = false
    )
    private Long idConsultas;

    // Consultorio propietario de la consulta.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false
    )
    private Consultorio consultorio;

    // Paciente atendido.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_pacientes",
            nullable = false
    )
    private Paciente paciente;

    // Historia clínica a la que pertenece la consulta.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_historias_clinicas",
            nullable = false
    )
    private HistoriaClinica historiaClinica;

    // Profesional responsable obtenido desde el JWT.
    @Column(
            name = "id_usuarios_medico",
            nullable = false,
            updatable = false
    )
    private Long idUsuariosMedico;

    // Cita relacionada, si la atención nació desde agenda.
    @Column(name = "id_citas")
    private Long idCitas;

    // Fecha y hora de la atención.
    @Column(
            name = "fecha_hora_atencion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaHoraAtencion;

    // Razón principal por la que consulta el paciente.
    @Column(
            name = "motivo_consulta",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String motivoConsulta;

    // Relato clínico y evolución del problema.
    @Column(
            name = "anamnesis",
            columnDefinition = "TEXT"
    )
    private String anamnesis;

    // Hallazgos del examen médico.
    @Column(
            name = "examen_fisico",
            columnDefinition = "TEXT"
    )
    private String examenFisico;

    // Análisis y valoración profesional.
    @Column(
            name = "evaluacion_clinica",
            columnDefinition = "TEXT"
    )
    private String evaluacionClinica;

    // Conducta terapéutica indicada.
    @Column(
            name = "plan_tratamiento",
            columnDefinition = "TEXT"
    )
    private String planTratamiento;

    // Indicaciones y seguimiento del paciente.
    @Column(
            name = "recomendaciones",
            columnDefinition = "TEXT"
    )
    private String recomendaciones;

    // Estado lógico de la consulta.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoConsulta estado;

    // Razón obligatoria cuando una consulta se anula.
    @Column(
            name = "motivo_anulacion",
            length = 500
    )
    private String motivoAnulacion;

    // Fecha de anulación.
    @Column(name = "fecha_anulacion")
    private OffsetDateTime fechaAnulacion;

    // Usuario que anuló la consulta.
    @Column(name = "anulado_por")
    private Long anuladoPor;

    // Fecha administrada por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Fecha de la última modificación.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime fechaModificacion;

    // Usuario que registró la consulta.
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
    protected Consulta() {
    }

    // Registra una nueva atención médica.
    public Consulta(
            final Consultorio consultorio,
            final Paciente paciente,
            final HistoriaClinica historiaClinica,
            final Long idUsuariosMedico,
            final Long idCitas,
            final String motivoConsulta,
            final String anamnesis,
            final String examenFisico,
            final String evaluacionClinica,
            final String planTratamiento,
            final String recomendaciones
    ) {
        this.consultorio = Objects.requireNonNull(
                consultorio,
                "El consultorio es obligatorio."
        );

        this.paciente = Objects.requireNonNull(
                paciente,
                "El paciente es obligatorio."
        );

        this.historiaClinica = Objects.requireNonNull(
                historiaClinica,
                "La historia clínica es obligatoria."
        );

        validarRelacionesClinicas(
                consultorio,
                paciente,
                historiaClinica
        );

        validarIdUsuario(idUsuariosMedico);

        validarIdOpcional(
                idCitas,
                "El identificador de la cita no es válido."
        );

        this.idUsuariosMedico =
                idUsuariosMedico;

        this.idCitas =
                idCitas;

        this.motivoConsulta =
                validarTextoObligatorio(
                        motivoConsulta,
                        "El motivo de consulta es obligatorio."
                );

        this.anamnesis =
                normalizarTextoClinico(anamnesis);

        this.examenFisico =
                normalizarTextoClinico(examenFisico);

        this.evaluacionClinica =
                normalizarTextoClinico(evaluacionClinica);

        this.planTratamiento =
                normalizarTextoClinico(planTratamiento);

        this.recomendaciones =
                normalizarTextoClinico(recomendaciones);

        this.estado =
                EstadoConsulta.ABIERTA;

        this.creadoPor =
                idUsuariosMedico;

        this.modificadoPor =
                idUsuariosMedico;
    }

    // Actualiza una consulta que todavía permanece abierta.
    public void actualizar(
            final Long idUsuarioResponsable,
            final String motivoConsulta,
            final String anamnesis,
            final String examenFisico,
            final String evaluacionClinica,
            final String planTratamiento,
            final String recomendaciones
    ) {
        validarIdUsuario(idUsuarioResponsable);
        validarConsultaEditable();

        this.motivoConsulta =
                validarTextoObligatorio(
                        motivoConsulta,
                        "El motivo de consulta es obligatorio."
                );

        this.anamnesis =
                normalizarTextoClinico(anamnesis);

        this.examenFisico =
                normalizarTextoClinico(examenFisico);

        this.evaluacionClinica =
                normalizarTextoClinico(evaluacionClinica);

        this.planTratamiento =
                normalizarTextoClinico(planTratamiento);

        this.recomendaciones =
                normalizarTextoClinico(recomendaciones);

        this.modificadoPor =
                idUsuarioResponsable;

        this.fechaModificacion =
                OffsetDateTime.now();
    }

    // Finaliza una consulta para proteger su contenido clínico.
    public void cerrar(
            final Long idUsuarioResponsable
    ) {
        validarIdUsuario(idUsuarioResponsable);

        if (estado != EstadoConsulta.ABIERTA) {
            throw new IllegalStateException(
                    "Solo puede cerrarse una consulta abierta."
            );
        }

        this.estado =
                EstadoConsulta.CERRADA;

        this.modificadoPor =
                idUsuarioResponsable;

        this.fechaModificacion =
                OffsetDateTime.now();
    }

    // Anula la consulta sin eliminar sus datos.
    public void anular(
            final Long idUsuarioResponsable,
            final String motivo
    ) {
        validarIdUsuario(idUsuarioResponsable);

        if (estado == EstadoConsulta.ANULADA) {
            throw new IllegalStateException(
                    "La consulta ya se encuentra anulada."
            );
        }

        final String motivoSeguro =
                validarMotivoAnulacion(motivo);

        this.estado =
                EstadoConsulta.ANULADA;

        this.motivoAnulacion =
                motivoSeguro;

        this.fechaAnulacion =
                OffsetDateTime.now();

        this.anuladoPor =
                idUsuarioResponsable;

        this.modificadoPor =
                idUsuarioResponsable;

        this.fechaModificacion =
                OffsetDateTime.now();
    }

    // Impide modificar consultas cerradas o anuladas.
    private void validarConsultaEditable() {
        if (estado != EstadoConsulta.ABIERTA) {
            throw new IllegalStateException(
                    "La consulta ya no puede modificarse."
            );
        }
    }

    // Verifica paciente, historia y consultorio.
    private static void validarRelacionesClinicas(
            final Consultorio consultorio,
            final Paciente paciente,
            final HistoriaClinica historiaClinica
    ) {
        if (paciente.getConsultorio() == null
                || historiaClinica.getConsultorio() == null
                || historiaClinica.getPaciente() == null) {
            throw new IllegalArgumentException(
                    "Las relaciones clínicas están incompletas."
            );
        }

        final Long idConsultorios =
                consultorio.getIdConsultorios();

        final Long idConsultoriosPaciente =
                paciente
                        .getConsultorio()
                        .getIdConsultorios();

        final Long idConsultoriosHistoria =
                historiaClinica
                        .getConsultorio()
                        .getIdConsultorios();

        final Long idPacientes =
                paciente.getIdPacientes();

        final Long idPacientesHistoria =
                historiaClinica
                        .getPaciente()
                        .getIdPacientes();

        if (!entidadesCoinciden(
                consultorio,
                paciente.getConsultorio(),
                idConsultorios,
                idConsultoriosPaciente
        )) {
            throw new IllegalArgumentException(
                    "El paciente no pertenece al consultorio."
            );
        }

        if (!entidadesCoinciden(
                consultorio,
                historiaClinica.getConsultorio(),
                idConsultorios,
                idConsultoriosHistoria
        )) {
            throw new IllegalArgumentException(
                    "La historia clínica no pertenece al consultorio."
            );
        }

        if (!entidadesCoinciden(
                paciente,
                historiaClinica.getPaciente(),
                idPacientes,
                idPacientesHistoria
        )) {
            throw new IllegalArgumentException(
                    "La historia clínica no pertenece al paciente."
            );
        }
    }

    // Compara entidades persistidas o referencias nuevas.
    private static boolean entidadesCoinciden(
            final Object primeraEntidad,
            final Object segundaEntidad,
            final Long primerId,
            final Long segundoId
    ) {
        if (primerId != null && segundoId != null) {
            return Objects.equals(primerId, segundoId);
        }

        return primeraEntidad == segundaEntidad;
    }

    // Valida el profesional responsable.
    private static void validarIdUsuario(
            final Long idUsuario
    ) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El profesional responsable es obligatorio."
            );
        }
    }

    // Valida una FK opcional.
    private static void validarIdOpcional(
            final Long identificador,
            final String mensaje
    ) {
        if (identificador != null && identificador <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    // Valida contenido clínico obligatorio.
    private static String validarTextoObligatorio(
            final String valor,
            final String mensaje
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }

        return validarLongitudTexto(valor.trim());
    }

    // Normaliza campos clínicos opcionales.
    private static String normalizarTextoClinico(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return validarLongitudTexto(valor.trim());
    }

    // Limita el tamaño para evitar cargas excesivas.
    private static String validarLongitudTexto(
            final String texto
    ) {
        if (texto.length()
                > LONGITUD_MAXIMA_TEXTO_CLINICO) {
            throw new IllegalArgumentException(
                    "El texto clínico no puede superar los "
                            + LONGITUD_MAXIMA_TEXTO_CLINICO
                            + " caracteres."
            );
        }

        return texto;
    }

    // Exige una justificación al anular.
    private static String validarMotivoAnulacion(
            final String motivo
    ) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo de anulación es obligatorio."
            );
        }

        final String motivoSeguro =
                motivo.trim();

        if (motivoSeguro.length() < 5
                || motivoSeguro.length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo de anulación debe contener "
                            + "entre 5 y 500 caracteres."
            );
        }

        return motivoSeguro;
    }

    public Long getIdConsultas() {
        return idConsultas;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public Long getIdUsuariosMedico() {
        return idUsuariosMedico;
    }

    public Long getIdCitas() {
        return idCitas;
    }

    public OffsetDateTime getFechaHoraAtencion() {
        return fechaHoraAtencion;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public String getExamenFisico() {
        return examenFisico;
    }

    public String getEvaluacionClinica() {
        return evaluacionClinica;
    }

    public String getPlanTratamiento() {
        return planTratamiento;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public EstadoConsulta getEstado() {
        return estado;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public OffsetDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public Long getAnuladoPor() {
        return anuladoPor;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
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

    // Compara consultas mediante su PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Consulta consulta)) {
            return false;
        }

        return idConsultas != null
                && Objects.equals(
                idConsultas,
                consulta.idConsultas
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Estados permitidos para una consulta.
     */
    public enum EstadoConsulta {
        ABIERTA,
        CERRADA,
        ANULADA
    }
}