package com.zenticode.medical.diagnosticos.entity;

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
import java.util.Locale;
import java.util.Objects;

/**
 * Representa un diagnóstico asociado a una consulta médica.
 */
@Entity
@Table(name = "diagnosticos_consultas")
public class DiagnosticoConsulta {

    // Clave primaria del diagnóstico.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_diagnosticos_consultas",
            nullable = false
    )
    private Long idDiagnosticosConsultas;

    // Consultorio propietario del diagnóstico.
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

    // Consulta asociada al diagnóstico.
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

    // Código CIE-10 indicado por el profesional.
    @Column(
            name = "codigo_cie10",
            length = 12
    )
    private String codigoCie10;

    // Descripción clínica del diagnóstico.
    @Column(
            name = "descripcion",
            nullable = false,
            length = 500
    )
    private String descripcion;

    // Nivel de confirmación del diagnóstico.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo",
            nullable = false,
            length = 20
    )
    private TipoDiagnostico tipo;

    // Indica si es el diagnóstico principal.
    @Column(
            name = "principal",
            nullable = false
    )
    private boolean principal;

    // Estado lógico del diagnóstico.
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoDiagnostico estado;

    // Fecha inicial administrada por PostgreSQL.
    @Column(
            name = "fecha_registro",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaRegistro;

    // Usuario que registró el diagnóstico.
    @Column(
            name = "registrado_por",
            nullable = false,
            updatable = false
    )
    private Long registradoPor;

    // Fecha de la última modificación.
    @Column(
            name = "fecha_modificacion",
            nullable = false,
            insertable = false
    )
    private OffsetDateTime fechaModificacion;

    // Usuario que realizó la última modificación.
    @Column(
            name = "modificado_por",
            nullable = false
    )
    private Long modificadoPor;

    // Justificación de la desactivación.
    @Column(
            name = "motivo_desactivacion",
            length = 500
    )
    private String motivoDesactivacion;

    // Fecha de desactivación.
    @Column(name = "fecha_desactivacion")
    private OffsetDateTime fechaDesactivacion;

    // Usuario que desactivó el diagnóstico.
    @Column(name = "desactivado_por")
    private Long desactivadoPor;

    // Constructor requerido por JPA.
    protected DiagnosticoConsulta() {
    }

    // Registra un diagnóstico nuevo.
    public DiagnosticoConsulta(
            final Consultorio consultorio,
            final Consulta consulta,
            final Long idUsuarioResponsable,
            final String codigoCie10,
            final String descripcion,
            final TipoDiagnostico tipo,
            final boolean principal
    ) {
        this.consultorio = Objects.requireNonNull(
                consultorio,
                "El consultorio es obligatorio."
        );

        this.consulta = Objects.requireNonNull(
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

        this.codigoCie10 =
                normalizarCodigoCie10(
                        codigoCie10
                );

        this.descripcion =
                validarDescripcion(
                        descripcion
                );

        this.tipo = Objects.requireNonNull(
                tipo,
                "El tipo de diagnóstico es obligatorio."
        );

        this.principal =
                principal;

        this.estado =
                EstadoDiagnostico.ACTIVO;

        this.registradoPor =
                idUsuarioResponsable;

        this.modificadoPor =
                idUsuarioResponsable;
    }

    // Actualiza un diagnóstico activo.
    public void actualizar(
            final Long idUsuarioResponsable,
            final String codigoCie10,
            final String descripcion,
            final TipoDiagnostico tipo,
            final boolean principal
    ) {
        validarActivo();
        validarIdUsuario(idUsuarioResponsable);

        this.codigoCie10 =
                normalizarCodigoCie10(
                        codigoCie10
                );

        this.descripcion =
                validarDescripcion(
                        descripcion
                );

        this.tipo = Objects.requireNonNull(
                tipo,
                "El tipo de diagnóstico es obligatorio."
        );

        this.principal =
                principal;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Marca el diagnóstico como principal.
    public void marcarComoPrincipal(
            final Long idUsuarioResponsable
    ) {
        validarActivo();
        validarIdUsuario(idUsuarioResponsable);

        this.principal = true;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Retira la condición de principal.
    public void quitarComoPrincipal(
            final Long idUsuarioResponsable
    ) {
        validarActivo();
        validarIdUsuario(idUsuarioResponsable);

        this.principal = false;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Desactiva sin eliminar el diagnóstico.
    public void desactivar(
            final Long idUsuarioResponsable,
            final String motivo
    ) {
        validarActivo();
        validarIdUsuario(idUsuarioResponsable);

        final String motivoSeguro =
                validarMotivoDesactivacion(
                        motivo
                );

        this.estado =
                EstadoDiagnostico.INACTIVO;

        this.principal =
                false;

        this.motivoDesactivacion =
                motivoSeguro;

        this.fechaDesactivacion =
                OffsetDateTime.now();

        this.desactivadoPor =
                idUsuarioResponsable;

        registrarModificacion(
                idUsuarioResponsable
        );
    }

    // Registra la última modificación.
    private void registrarModificacion(
            final Long idUsuarioResponsable
    ) {
        this.modificadoPor =
                idUsuarioResponsable;

        this.fechaModificacion =
                OffsetDateTime.now();
    }

    // Verifica que consulta y consultorio coincidan.
    private static void validarMismoConsultorio(
            final Consultorio consultorio,
            final Consulta consulta
    ) {
        if (consulta.getConsultorio() == null) {
            throw new IllegalArgumentException(
                    "La consulta no tiene "
                            + "un consultorio válido."
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

        if (consulta.getConsultorio() != consultorio) {
            throw new IllegalArgumentException(
                    "La consulta no pertenece al consultorio."
            );
        }
    }

    // Evita modificar diagnósticos inactivos.
    private void validarActivo() {
        if (estado != EstadoDiagnostico.ACTIVO) {
            throw new IllegalStateException(
                    "El diagnóstico ya no se encuentra activo."
            );
        }
    }

    // Valida al usuario responsable.
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

    // Normaliza el código CIE-10.
    private static String normalizarCodigoCie10(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String codigo =
                valor
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "");

        if (codigo.length() > 12) {
            throw new IllegalArgumentException(
                    "El código CIE-10 no puede superar "
                            + "los 12 caracteres."
            );
        }

        if (!codigo.matches(
                "^[A-Z][0-9]{2}(\\.[A-Z0-9]{1,4})?$"
        )) {
            throw new IllegalArgumentException(
                    "El código CIE-10 no tiene "
                            + "un formato válido."
            );
        }

        return codigo;
    }

    // Valida la descripción clínica.
    private static String validarDescripcion(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción del diagnóstico "
                            + "es obligatoria."
            );
        }

        final String descripcionSegura =
                valor.trim();

        if (descripcionSegura.length() < 3
                || descripcionSegura.length() > 500) {
            throw new IllegalArgumentException(
                    "La descripción del diagnóstico debe contener "
                            + "entre 3 y 500 caracteres."
            );
        }

        return descripcionSegura;
    }

    // Valida la justificación de desactivación.
    private static String validarMotivoDesactivacion(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo de desactivación es obligatorio."
            );
        }

        final String motivoSeguro =
                valor.trim();

        if (motivoSeguro.length() < 5
                || motivoSeguro.length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo de desactivación debe contener "
                            + "entre 5 y 500 caracteres."
            );
        }

        return motivoSeguro;
    }

    public Long getIdDiagnosticosConsultas() {
        return idDiagnosticosConsultas;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public String getCodigoCie10() {
        return codigoCie10;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoDiagnostico getTipo() {
        return tipo;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public EstadoDiagnostico getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public OffsetDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Long getModificadoPor() {
        return modificadoPor;
    }

    public String getMotivoDesactivacion() {
        return motivoDesactivacion;
    }

    public OffsetDateTime getFechaDesactivacion() {
        return fechaDesactivacion;
    }

    public Long getDesactivadoPor() {
        return desactivadoPor;
    }

    // Compara mediante la PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof DiagnosticoConsulta diagnostico)) {
            return false;
        }

        return idDiagnosticosConsultas != null
                && Objects.equals(
                idDiagnosticosConsultas,
                diagnostico.idDiagnosticosConsultas
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Nivel de confirmación del diagnóstico.
     */
    public enum TipoDiagnostico {
        PRESUNTIVO,
        DEFINITIVO
    }

    /**
     * Estado lógico del diagnóstico.
     */
    public enum EstadoDiagnostico {
        ACTIVO,
        INACTIVO
    }
}