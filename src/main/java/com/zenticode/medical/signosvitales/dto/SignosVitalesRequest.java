package com.zenticode.medical.signosvitales.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Mediciones registradas durante una consulta médica.
 */
public record SignosVitalesRequest(

        @DecimalMin(
                value = "20.0",
                message = "La temperatura debe ser "
                        + "igual o mayor que 20 °C."
        )
        @DecimalMax(
                value = "50.0",
                message = "La temperatura debe ser "
                        + "igual o menor que 50 °C."
        )
        @Digits(
                integer = 2,
                fraction = 1,
                message = "La temperatura debe tener "
                        + "como máximo un decimal."
        )
        BigDecimal temperaturaC,

        @Min(
                value = 40,
                message = "La presión sistólica debe ser "
                        + "igual o mayor que 40 mmHg."
        )
        @Max(
                value = 300,
                message = "La presión sistólica debe ser "
                        + "igual o menor que 300 mmHg."
        )
        Short presionSistolicaMmhg,

        @Min(
                value = 20,
                message = "La presión diastólica debe ser "
                        + "igual o mayor que 20 mmHg."
        )
        @Max(
                value = 200,
                message = "La presión diastólica debe ser "
                        + "igual o menor que 200 mmHg."
        )
        Short presionDiastolicaMmhg,

        @Min(
                value = 20,
                message = "La frecuencia cardíaca debe ser "
                        + "igual o mayor que 20 lpm."
        )
        @Max(
                value = 300,
                message = "La frecuencia cardíaca debe ser "
                        + "igual o menor que 300 lpm."
        )
        Short frecuenciaCardiacaLpm,

        @Min(
                value = 5,
                message = "La frecuencia respiratoria debe ser "
                        + "igual o mayor que 5 rpm."
        )
        @Max(
                value = 100,
                message = "La frecuencia respiratoria debe ser "
                        + "igual o menor que 100 rpm."
        )
        Short frecuenciaRespiratoriaRpm,

        @DecimalMin(
                value = "0.0",
                message = "La saturación de oxígeno "
                        + "no puede ser negativa."
        )
        @DecimalMax(
                value = "100.0",
                message = "La saturación de oxígeno "
                        + "no puede superar el 100 %."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "La saturación de oxígeno debe tener "
                        + "como máximo dos decimales."
        )
        BigDecimal saturacionOxigenoPct,

        @DecimalMin(
                value = "0.10",
                message = "El peso debe ser "
                        + "igual o mayor que 0.10 kg."
        )
        @DecimalMax(
                value = "500.00",
                message = "El peso debe ser "
                        + "igual o menor que 500 kg."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "El peso debe tener "
                        + "como máximo dos decimales."
        )
        BigDecimal pesoKg,

        @DecimalMin(
                value = "20.00",
                message = "La talla debe ser "
                        + "igual o mayor que 20 cm."
        )
        @DecimalMax(
                value = "300.00",
                message = "La talla debe ser "
                        + "igual o menor que 300 cm."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "La talla debe tener "
                        + "como máximo dos decimales."
        )
        BigDecimal tallaCm,

        @DecimalMin(
                value = "10.00",
                message = "El perímetro abdominal debe ser "
                        + "igual o mayor que 10 cm."
        )
        @DecimalMax(
                value = "300.00",
                message = "El perímetro abdominal debe ser "
                        + "igual o menor que 300 cm."
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "El perímetro abdominal debe tener "
                        + "como máximo dos decimales."
        )
        BigDecimal perimetroAbdominalCm,

        @Size(
                max = 500,
                message = "Las observaciones no pueden superar "
                        + "los 500 caracteres."
        )
        String observaciones

) {

    // Normaliza y valida la relación entre las mediciones.
    public SignosVitalesRequest {
        observaciones =
                normalizarObservaciones(observaciones);

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

        validarPresionArterial(
                presionSistolicaMmhg,
                presionDiastolicaMmhg
        );
    }

    // Evita crear registros clínicos completamente vacíos.
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

    // Exige ambas cifras y una relación técnicamente coherente.
    private static void validarPresionArterial(
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

    // Convierte observaciones vacías en null.
    private static String normalizarObservaciones(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}