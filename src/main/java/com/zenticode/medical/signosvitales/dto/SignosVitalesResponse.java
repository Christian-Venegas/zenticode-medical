package com.zenticode.medical.signosvitales.dto;

import com.zenticode.medical.signosvitales.entity.SignosVitales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Información controlada de un registro de signos vitales.
 */
public record SignosVitalesResponse(

        Long idSignosVitales,

        Long idConsultorios,

        Long idConsultas,

        Long idPacientes,

        BigDecimal temperaturaC,

        Short presionSistolicaMmhg,

        Short presionDiastolicaMmhg,

        Short frecuenciaCardiacaLpm,

        Short frecuenciaRespiratoriaRpm,

        BigDecimal saturacionOxigenoPct,

        BigDecimal pesoKg,

        BigDecimal tallaCm,

        BigDecimal perimetroAbdominalCm,

        BigDecimal imc,

        String observaciones,

        OffsetDateTime fechaRegistro,

        Long registradoPor

) {

    // Convierte la entidad en una respuesta segura.
    public static SignosVitalesResponse desdeEntidad(
            final SignosVitales signosVitales
    ) {
        Objects.requireNonNull(
                signosVitales,
                "El registro de signos vitales es obligatorio."
        );

        Objects.requireNonNull(
                signosVitales.getConsultorio(),
                "El consultorio del registro es obligatorio."
        );

        Objects.requireNonNull(
                signosVitales.getConsulta(),
                "La consulta médica del registro es obligatoria."
        );

        Objects.requireNonNull(
                signosVitales.getConsulta().getPaciente(),
                "El paciente de la consulta es obligatorio."
        );

        return new SignosVitalesResponse(
                signosVitales.getIdSignosVitales(),
                signosVitales
                        .getConsultorio()
                        .getIdConsultorios(),
                signosVitales
                        .getConsulta()
                        .getIdConsultas(),
                signosVitales
                        .getConsulta()
                        .getPaciente()
                        .getIdPacientes(),
                signosVitales.getTemperaturaC(),
                signosVitales.getPresionSistolicaMmhg(),
                signosVitales.getPresionDiastolicaMmhg(),
                signosVitales.getFrecuenciaCardiacaLpm(),
                signosVitales.getFrecuenciaRespiratoriaRpm(),
                signosVitales.getSaturacionOxigenoPct(),
                signosVitales.getPesoKg(),
                signosVitales.getTallaCm(),
                signosVitales.getPerimetroAbdominalCm(),
                calcularImc(
                        signosVitales.getPesoKg(),
                        signosVitales.getTallaCm()
                ),
                signosVitales.getObservaciones(),
                signosVitales.getFechaRegistro(),
                signosVitales.getRegistradoPor()
        );
    }

    // Calcula el IMC únicamente cuando existen peso y talla.
    private static BigDecimal calcularImc(
            final BigDecimal pesoKg,
            final BigDecimal tallaCm
    ) {
        if (pesoKg == null || tallaCm == null) {
            return null;
        }

        if (pesoKg.compareTo(BigDecimal.ZERO) <= 0
                || tallaCm.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // Convierte centímetros a metros.
        final BigDecimal tallaMetros =
                tallaCm.divide(
                        new BigDecimal("100"),
                        4,
                        RoundingMode.HALF_UP
                );

        final BigDecimal tallaAlCuadrado =
                tallaMetros.multiply(tallaMetros);

        if (tallaAlCuadrado.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return pesoKg.divide(
                tallaAlCuadrado,
                2,
                RoundingMode.HALF_UP
        );
    }
}