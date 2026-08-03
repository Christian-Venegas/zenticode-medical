package com.zenticode.medical.recetas.entity;

import com.zenticode.medical.consultas.entity.Consulta;
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
import java.util.Objects;

/**
 * Representa una receta emitida durante una atención médica.
 */
@Entity
@Table(name = "recetas")
public class Receta {

    // Clave primaria de la receta.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_recetas",
            nullable = false
    )
    private Long idRecetas;

    // Consultorio propietario de la receta.
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

    // Consulta médica asociada.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultas",
            nullable = false,
            updatable = false
    )
    private Consulta consulta;

    // Fecha de emisión administrada por PostgreSQL.
    @Column(
            name = "fecha_emision",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaEmision;

    // Indicaciones generales para toda la receta.
    @Column(
            name = "indicaciones_generales",
            columnDefinition = "TEXT"
    )
    private String indicacionesGenerales;

    // Estado lógico de la receta.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoReceta estado;

    // Justificación obligatoria cuando se anula.
    @Column(
            name = "motivo_anulacion",
            length = 500
    )
    private String motivoAnulacion;

    // Fecha exacta de anulación.
    @Column(name = "fecha_anulacion")
    private OffsetDateTime fechaAnulacion;

    // Usuario responsable de la anulación.
    @Column(name = "anulado_por")
    private Long anuladoPor;

    // Profesional que emitió la receta.
    @Column(
            name = "emitido_por",
            nullable = false,
            updatable = false
    )
    private Long emitidoPor;

    // Fecha de creación administrada por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Constructor requerido por JPA.
    protected Receta() {
    }

    // Emite una receta vinculada a una consulta médica.
    public Receta(
            final Consultorio consultorio,
            final Consulta consulta,
            final Long idUsuarioResponsable,
            final String indicacionesGenerales
    ) {
        this.consultorio =
                Objects.requireNonNull(
                        consultorio,
                        "El consultorio es obligatorio."
                );

        this.consulta =
                Objects.requireNonNull(
                        consulta,
                        "La consulta médica es obligatoria."
                );

        validarMismoConsultorio(
                consultorio,
                consulta
        );

        validarIdUsuario(
                idUsuarioResponsable
        );

        this.indicacionesGenerales =
                normalizarIndicacionesGenerales(
                        indicacionesGenerales
                );

        this.estado =
                EstadoReceta.EMITIDA;

        this.emitidoPor =
                idUsuarioResponsable;

        this.motivoAnulacion =
                null;

        this.fechaAnulacion =
                null;

        this.anuladoPor =
                null;
    }

    // Anula la receta sin eliminar su información.
    public void anular(
            final Long idUsuarioResponsable,
            final String motivo
    ) {
        validarIdUsuario(
                idUsuarioResponsable
        );

        validarRecetaEmitida();

        final String motivoSeguro =
                validarMotivoAnulacion(
                        motivo
                );

        this.motivoAnulacion =
                motivoSeguro;

        this.fechaAnulacion =
                OffsetDateTime.now();

        this.anuladoPor =
                idUsuarioResponsable;

        this.estado =
                EstadoReceta.ANULADA;
    }

    // Impide anular una receta que ya no está emitida.
    private void validarRecetaEmitida() {
        if (estado == EstadoReceta.ANULADA) {
            throw new IllegalStateException(
                    "La receta ya se encuentra anulada."
            );
        }

        if (estado != EstadoReceta.EMITIDA) {
            throw new IllegalStateException(
                    "La receta no está disponible para anulación."
            );
        }
    }

    // Comprueba que consulta y consultorio coincidan.
    private static void validarMismoConsultorio(
            final Consultorio consultorio,
            final Consulta consulta
    ) {
        if (consulta.getConsultorio() == null) {
            throw new IllegalArgumentException(
                    "La consulta no tiene un consultorio válido."
            );
        }

        final Long idConsultorios =
                consultorio.getIdConsultorios();

        final Long idConsultoriosConsulta =
                consulta
                        .getConsultorio()
                        .getIdConsultorios();

        if (idConsultorios != null
                && idConsultoriosConsulta != null) {

            if (!Objects.equals(
                    idConsultorios,
                    idConsultoriosConsulta
            )) {
                throw new IllegalArgumentException(
                        "La consulta no pertenece al consultorio."
                );
            }

            return;
        }

        // Protege entidades nuevas aún no persistidas.
        if (consulta.getConsultorio() != consultorio) {
            throw new IllegalArgumentException(
                    "La consulta no pertenece al consultorio."
            );
        }
    }

    // Valida el profesional responsable.
    private static void validarIdUsuario(
            final Long idUsuarioResponsable
    ) {
        if (idUsuarioResponsable == null
                || idUsuarioResponsable <= 0) {
            throw new IllegalArgumentException(
                    "El profesional responsable es obligatorio."
            );
        }
    }

    // Normaliza indicaciones generales opcionales.
    private static String normalizarIndicacionesGenerales(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String indicaciones =
                valor.trim();

        if (indicaciones.length() > 10000) {
            throw new IllegalArgumentException(
                    "Las indicaciones generales no pueden superar "
                            + "los 10000 caracteres."
            );
        }

        return indicaciones;
    }

    // Valida la justificación de anulación.
    private static String validarMotivoAnulacion(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo de anulación es obligatorio."
            );
        }

        final String motivo =
                valor.trim();

        if (motivo.length() < 5
                || motivo.length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo de anulación debe contener "
                            + "entre 5 y 500 caracteres."
            );
        }

        return motivo;
    }

    public Long getIdRecetas() {
        return idRecetas;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public OffsetDateTime getFechaEmision() {
        return fechaEmision;
    }

    public String getIndicacionesGenerales() {
        return indicacionesGenerales;
    }

    public EstadoReceta getEstado() {
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

    public Long getEmitidoPor() {
        return emitidoPor;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    // Compara recetas mediante su PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Receta receta)) {
            return false;
        }

        return idRecetas != null
                && Objects.equals(
                idRecetas,
                receta.idRecetas
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Estados lógicos permitidos para una receta.
     */
    public enum EstadoReceta {
        EMITIDA,
        ANULADA
    }
}