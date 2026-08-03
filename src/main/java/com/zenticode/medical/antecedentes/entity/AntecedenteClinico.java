package com.zenticode.medical.antecedentes.entity;

import com.zenticode.medical.historias.entity.HistoriaClinica;
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
import java.util.Objects;

/**
 * Representa un antecedente de la historia clínica.
 */
@Entity
@Table(name = "antecedentes_clinicos")
public class AntecedenteClinico {

    // Clave primaria del antecedente.
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id_antecedentes_clinicos",
            nullable = false
    )
    private Long idAntecedentesClinicos;

    // Historia clínica propietaria.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_historias_clinicas",
            nullable = false,
            updatable = false
    )
    private HistoriaClinica historiaClinica;

    // Categoría médica del antecedente.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo",
            nullable = false,
            length = 30
    )
    private TipoAntecedente tipo;

    // Descripción principal.
    @Column(
            name = "descripcion",
            nullable = false,
            length = 1000
    )
    private String descripcion;

    // Fecha informada o aproximada.
    @Column(name = "fecha_aproximada")
    private LocalDate fechaAproximada;

    // Información complementaria.
    @Column(
            name = "observaciones",
            length = 1000
    )
    private String observaciones;

    // Nivel de importancia clínica.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "importancia",
            nullable = false,
            length = 20
    )
    private NivelImportancia importancia;

    // Estado lógico del antecedente.
    @Column(
            name = "activo",
            nullable = false
    )
    private boolean activo;

    // Usuario que registró el antecedente.
    @Column(
            name = "creado_por",
            nullable = false,
            updatable = false
    )
    private Long creadoPor;

    // Usuario de la última modificación.
    @Column(
            name = "modificado_por",
            nullable = false
    )
    private Long modificadoPor;

    // Fecha de creación administrada por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Fecha de modificación.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime fechaModificacion;

    // Constructor requerido por JPA.
    protected AntecedenteClinico() {
    }

    // Registra un antecedente activo.
    public AntecedenteClinico(
            final HistoriaClinica historiaClinica,
            final TipoAntecedente tipo,
            final String descripcion,
            final LocalDate fechaAproximada,
            final String observaciones,
            final NivelImportancia importancia,
            final Long idUsuarioResponsable
    ) {
        this.historiaClinica =
                Objects.requireNonNull(
                        historiaClinica,
                        "La historia clínica es obligatoria."
                );

        validarHistoriaActiva(
                historiaClinica
        );

        this.tipo =
                Objects.requireNonNull(
                        tipo,
                        "El tipo de antecedente es obligatorio."
                );

        this.descripcion =
                normalizarTextoObligatorio(
                        descripcion,
                        1000,
                        "La descripción del antecedente "
                                + "es obligatoria."
                );

        this.fechaAproximada =
                validarFechaAproximada(
                        fechaAproximada
                );

        this.observaciones =
                normalizarTextoOpcional(
                        observaciones,
                        1000
                );

        this.importancia =
                importancia != null
                        ? importancia
                        : NivelImportancia.MEDIA;

        validarIdUsuario(
                idUsuarioResponsable
        );

        this.activo = true;
        this.creadoPor = idUsuarioResponsable;
        this.modificadoPor = idUsuarioResponsable;
    }

    // Actualiza la información del antecedente.
    public void actualizar(
            final TipoAntecedente tipo,
            final String descripcion,
            final LocalDate fechaAproximada,
            final String observaciones,
            final NivelImportancia importancia,
            final Long idUsuarioResponsable
    ) {
        verificarActivo();
        validarHistoriaActiva(
                historiaClinica
        );

        this.tipo =
                Objects.requireNonNull(
                        tipo,
                        "El tipo de antecedente es obligatorio."
                );

        this.descripcion =
                normalizarTextoObligatorio(
                        descripcion,
                        1000,
                        "La descripción del antecedente "
                                + "es obligatoria."
                );

        this.fechaAproximada =
                validarFechaAproximada(
                        fechaAproximada
                );

        this.observaciones =
                normalizarTextoOpcional(
                        observaciones,
                        1000
                );

        this.importancia =
                importancia != null
                        ? importancia
                        : NivelImportancia.MEDIA;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Desactiva sin eliminar información clínica.
    public void desactivar(
            final Long idUsuarioResponsable
    ) {
        if (!activo) {
            return;
        }

        validarHistoriaActiva(
                historiaClinica
        );

        this.activo = false;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Reactiva un antecedente desactivado.
    public void reactivar(
            final Long idUsuarioResponsable
    ) {
        if (activo) {
            return;
        }

        validarHistoriaActiva(
                historiaClinica
        );

        this.activo = true;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Indica si el antecedente está disponible.
    public boolean estaActivo() {
        return activo;
    }

    // Evita modificar un antecedente desactivado.
    private void verificarActivo() {
        if (!activo) {
            throw new IllegalStateException(
                    "El antecedente clínico "
                            + "se encuentra inactivo."
            );
        }
    }

    // Comprueba que la historia admita modificaciones.
    private static void validarHistoriaActiva(
            final HistoriaClinica historiaClinica
    ) {
        if (!historiaClinica.estaActiva()) {
            throw new IllegalStateException(
                    "La historia clínica debe estar activa."
            );
        }
    }

    // Registra al responsable y la fecha del cambio.
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

    // Valida el usuario responsable.
    private static void validarIdUsuario(
            final Long idUsuarioResponsable
    ) {
        if (
                idUsuarioResponsable == null
                        || idUsuarioResponsable <= 0
        ) {
            throw new IllegalArgumentException(
                    "El profesional responsable "
                            + "es obligatorio."
            );
        }
    }

    // Evita registrar fechas futuras.
    private static LocalDate validarFechaAproximada(
            final LocalDate fechaAproximada
    ) {
        if (
                fechaAproximada != null
                        && fechaAproximada.isAfter(
                        LocalDate.now()
                )
        ) {
            throw new IllegalArgumentException(
                    "La fecha aproximada no puede "
                            + "estar en el futuro."
            );
        }

        return fechaAproximada;
    }

    // Normaliza un texto obligatorio.
    private static String normalizarTextoObligatorio(
            final String valor,
            final int longitudMaxima,
            final String mensajeObligatorio
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            throw new IllegalArgumentException(
                    mensajeObligatorio
            );
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    "El texto no puede superar los "
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
        if (
                valor == null
                        || valor.isBlank()
        ) {
            return null;
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    "El texto no puede superar los "
                            + longitudMaxima
                            + " caracteres."
            );
        }

        return texto;
    }

    public Long getIdAntecedentesClinicos() {
        return idAntecedentesClinicos;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public TipoAntecedente getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFechaAproximada() {
        return fechaAproximada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public NivelImportancia getImportancia() {
        return importancia;
    }

    public boolean isActivo() {
        return activo;
    }

    public Long getCreadoPor() {
        return creadoPor;
    }

    public Long getModificadoPor() {
        return modificadoPor;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    // Compara antecedentes mediante su PK.
    @Override
    public boolean equals(
            final Object objeto
    ) {
        if (this == objeto) {
            return true;
        }

        if (
                !(objeto instanceof
                        AntecedenteClinico antecedente)
        ) {
            return false;
        }

        return idAntecedentesClinicos != null
                && Objects.equals(
                idAntecedentesClinicos,
                antecedente.idAntecedentesClinicos
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Tipos permitidos por PostgreSQL.
     */
    public enum TipoAntecedente {
        PATOLOGICO,
        QUIRURGICO,
        FAMILIAR,
        FARMACOLOGICO,
        ALERGICO,
        HABITO,
        GINECO_OBSTETRICO,
        OTRO
    }

    /**
     * Importancia clínica del antecedente.
     */
    public enum NivelImportancia {
        BAJA,
        MEDIA,
        ALTA,
        CRITICA
    }
}