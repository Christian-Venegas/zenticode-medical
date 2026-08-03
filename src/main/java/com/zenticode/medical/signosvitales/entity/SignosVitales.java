package com.zenticode.medical.signosvitales.entity;

import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultorios.entity.Consultorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Representa las mediciones registradas durante una consulta.
 */
@Entity
@Table(name = "signos_vitales")
public class SignosVitales {

    // Clave primaria del registro de signos vitales.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_signos_vitales",
            nullable = false
    )
    private Long idSignosVitales;

    // Consultorio propietario de las mediciones.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false
    )
    private Consultorio consultorio;

    // Consulta médica asociada.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultas",
            nullable = false
    )
    private Consulta consulta;

    // Temperatura corporal expresada en grados Celsius.
    @Column(
            name = "temperatura_c",
            precision = 4,
            scale = 1
    )
    private BigDecimal temperaturaC;

    // Presión arterial sistólica expresada en mmHg.
    @Column(name = "presion_sistolica_mmhg")
    private Short presionSistolicaMmhg;

    // Presión arterial diastólica expresada en mmHg.
    @Column(name = "presion_diastolica_mmhg")
    private Short presionDiastolicaMmhg;

    // Frecuencia cardíaca expresada en latidos por minuto.
    @Column(name = "frecuencia_cardiaca_lpm")
    private Short frecuenciaCardiacaLpm;

    // Frecuencia respiratoria expresada en respiraciones por minuto.
    @Column(name = "frecuencia_respiratoria_rpm")
    private Short frecuenciaRespiratoriaRpm;

    // Saturación de oxígeno expresada como porcentaje.
    @Column(
            name = "saturacion_oxigeno_pct",
            precision = 5,
            scale = 2
    )
    private BigDecimal saturacionOxigenoPct;

    // Peso expresado en kilogramos.
    @Column(
            name = "peso_kg",
            precision = 6,
            scale = 2
    )
    private BigDecimal pesoKg;

    // Talla expresada en centímetros.
    @Column(
            name = "talla_cm",
            precision = 6,
            scale = 2
    )
    private BigDecimal tallaCm;

    // Perímetro abdominal expresado en centímetros.
    @Column(
            name = "perimetro_abdominal_cm",
            precision = 6,
            scale = 2
    )
    private BigDecimal perimetroAbdominalCm;

    // Observación descriptiva vinculada a la medición.
    @Column(
            name = "observaciones",
            length = 500
    )
    private String observaciones;

    // Fecha de registro administrada por PostgreSQL.
    @Column(
            name = "fecha_registro",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaRegistro;

    // Usuario que registró las mediciones.
    @Column(
            name = "registrado_por",
            nullable = false,
            updatable = false
    )
    private Long registradoPor;

    // Constructor requerido por JPA.
    protected SignosVitales() {
    }

    // Registra las mediciones de una consulta.
    public SignosVitales(
            final Consultorio consultorio,
            final Consulta consulta,
            final Long idUsuarioResponsable,
            final BigDecimal temperaturaC,
            final Short presionSistolicaMmhg,
            final Short presionDiastolicaMmhg,
            final Short frecuenciaCardiacaLpm,
            final Short frecuenciaRespiratoriaRpm,
            final BigDecimal saturacionOxigenoPct,
            final BigDecimal pesoKg,
            final BigDecimal tallaCm,
            final BigDecimal perimetroAbdominalCm,
            final String observaciones
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

        validarIdUsuario(idUsuarioResponsable);

        validarExisteAlgunaMedicion(
                temperaturaC,
                presionSistolicaMmhg,
                presionDiastolicaMmhg,
                frecuenciaCardiacaLpm,
                frecuenciaRespiratoriaRpm,
                saturacionOxigenoPct,
                pesoKg,
                tallaCm,
                perimetroAbdominalCm
        );

        this.temperaturaC =
                validarDecimalPositivo(
                        temperaturaC,
                        "La temperatura debe ser mayor que cero."
                );

        this.presionSistolicaMmhg =
                validarEnteroPositivo(
                        presionSistolicaMmhg,
                        "La presión sistólica debe ser mayor que cero."
                );

        this.presionDiastolicaMmhg =
                validarEnteroPositivo(
                        presionDiastolicaMmhg,
                        "La presión diastólica debe ser mayor que cero."
                );

        validarRelacionPresionArterial(
                presionSistolicaMmhg,
                presionDiastolicaMmhg
        );

        this.frecuenciaCardiacaLpm =
                validarEnteroPositivo(
                        frecuenciaCardiacaLpm,
                        "La frecuencia cardíaca debe ser mayor que cero."
                );

        this.frecuenciaRespiratoriaRpm =
                validarEnteroPositivo(
                        frecuenciaRespiratoriaRpm,
                        "La frecuencia respiratoria "
                                + "debe ser mayor que cero."
                );

        this.saturacionOxigenoPct =
                validarPorcentaje(
                        saturacionOxigenoPct,
                        "La saturación de oxígeno debe estar "
                                + "entre 0 y 100."
                );

        this.pesoKg =
                validarDecimalPositivo(
                        pesoKg,
                        "El peso debe ser mayor que cero."
                );

        this.tallaCm =
                validarDecimalPositivo(
                        tallaCm,
                        "La talla debe ser mayor que cero."
                );

        this.perimetroAbdominalCm =
                validarDecimalPositivo(
                        perimetroAbdominalCm,
                        "El perímetro abdominal debe ser "
                                + "mayor que cero."
                );

        this.observaciones =
                normalizarObservaciones(observaciones);

        this.registradoPor =
                idUsuarioResponsable;
    }

    // Verifica que la consulta pertenezca al consultorio.
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

        if (consulta.getConsultorio() != consultorio) {
            throw new IllegalArgumentException(
                    "La consulta no pertenece al consultorio."
            );
        }
    }

    // Exige al menos una medición para evitar registros vacíos.
    private static void validarExisteAlgunaMedicion(
            final BigDecimal temperaturaC,
            final Short presionSistolicaMmhg,
            final Short presionDiastolicaMmhg,
            final Short frecuenciaCardiacaLpm,
            final Short frecuenciaRespiratoriaRpm,
            final BigDecimal saturacionOxigenoPct,
            final BigDecimal pesoKg,
            final BigDecimal tallaCm,
            final BigDecimal perimetroAbdominalCm
    ) {
        final boolean sinMediciones =
                temperaturaC == null
                        && presionSistolicaMmhg == null
                        && presionDiastolicaMmhg == null
                        && frecuenciaCardiacaLpm == null
                        && frecuenciaRespiratoriaRpm == null
                        && saturacionOxigenoPct == null
                        && pesoKg == null
                        && tallaCm == null
                        && perimetroAbdominalCm == null;

        if (sinMediciones) {
            throw new IllegalArgumentException(
                    "Debe registrar al menos una medición."
            );
        }
    }

    // La presión sistólica debe acompañar a la diastólica.
    private static void validarRelacionPresionArterial(
            final Short presionSistolicaMmhg,
            final Short presionDiastolicaMmhg
    ) {
        final boolean soloSistolica =
                presionSistolicaMmhg != null
                        && presionDiastolicaMmhg == null;

        final boolean soloDiastolica =
                presionSistolicaMmhg == null
                        && presionDiastolicaMmhg != null;

        if (soloSistolica || soloDiastolica) {
            throw new IllegalArgumentException(
                    "La presión sistólica y diastólica "
                            + "deben registrarse juntas."
            );
        }

        if (presionSistolicaMmhg != null
                && presionDiastolicaMmhg != null
                && presionSistolicaMmhg
                <= presionDiastolicaMmhg) {
            throw new IllegalArgumentException(
                    "La presión sistólica debe ser mayor "
                            + "que la presión diastólica."
            );
        }
    }

    // Valida números decimales opcionales positivos.
    private static BigDecimal validarDecimalPositivo(
            final BigDecimal valor,
            final String mensaje
    ) {
        if (valor != null
                && valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(mensaje);
        }

        return valor;
    }

    // Valida números enteros opcionales positivos.
    private static Short validarEnteroPositivo(
            final Short valor,
            final String mensaje
    ) {
        if (valor != null && valor <= 0) {
            throw new IllegalArgumentException(mensaje);
        }

        return valor;
    }

    // Valida un porcentaje opcional.
    private static BigDecimal validarPorcentaje(
            final BigDecimal valor,
            final String mensaje
    ) {
        if (valor != null
                && (
                valor.compareTo(BigDecimal.ZERO) < 0
                        || valor.compareTo(
                        new BigDecimal("100")
                ) > 0
        )) {
            throw new IllegalArgumentException(mensaje);
        }

        return valor;
    }

    // Valida el usuario responsable obtenido desde el JWT.
    private static void validarIdUsuario(
            final Long idUsuarioResponsable
    ) {
        if (idUsuarioResponsable == null
                || idUsuarioResponsable <= 0) {
            throw new IllegalArgumentException(
                    "El usuario responsable es obligatorio."
            );
        }
    }

    // Convierte observaciones vacías en null.
    private static String normalizarObservaciones(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String observacion =
                valor.trim();

        if (observacion.length() > 500) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar "
                            + "los 500 caracteres."
            );
        }

        return observacion;
    }

    public Long getIdSignosVitales() {
        return idSignosVitales;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public Short getPresionSistolicaMmhg() {
        return presionSistolicaMmhg;
    }

    public Short getPresionDiastolicaMmhg() {
        return presionDiastolicaMmhg;
    }

    public Short getFrecuenciaCardiacaLpm() {
        return frecuenciaCardiacaLpm;
    }

    public Short getFrecuenciaRespiratoriaRpm() {
        return frecuenciaRespiratoriaRpm;
    }

    public BigDecimal getSaturacionOxigenoPct() {
        return saturacionOxigenoPct;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public BigDecimal getTallaCm() {
        return tallaCm;
    }

    public BigDecimal getPerimetroAbdominalCm() {
        return perimetroAbdominalCm;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    // Compara registros mediante su PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof SignosVitales signosVitales)) {
            return false;
        }

        return idSignosVitales != null
                && Objects.equals(
                idSignosVitales,
                signosVitales.idSignosVitales
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}