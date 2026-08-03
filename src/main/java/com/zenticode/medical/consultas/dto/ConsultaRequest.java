package com.zenticode.medical.consultas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para registrar o actualizar una consulta.
 */
public record ConsultaRequest(

        @Positive(
                message = "El identificador de la cita "
                        + "debe ser mayor que cero."
        )
        Long idCitas,

        @NotBlank(
                message = "El motivo de consulta es obligatorio."
        )
        @Size(
                min = 3,
                max = 20000,
                message = "El motivo de consulta debe contener "
                        + "entre 3 y 20000 caracteres."
        )
        String motivoConsulta,

        @Size(
                max = 20000,
                message = "La anamnesis no puede superar "
                        + "los 20000 caracteres."
        )
        String anamnesis,

        @Size(
                max = 20000,
                message = "El examen físico no puede superar "
                        + "los 20000 caracteres."
        )
        String examenFisico,

        @Size(
                max = 20000,
                message = "La evaluación clínica no puede superar "
                        + "los 20000 caracteres."
        )
        String evaluacionClinica,

        @Size(
                max = 20000,
                message = "El plan de tratamiento no puede superar "
                        + "los 20000 caracteres."
        )
        String planTratamiento,

        @Size(
                max = 20000,
                message = "Las recomendaciones no pueden superar "
                        + "los 20000 caracteres."
        )
        String recomendaciones

) {

    // Normaliza textos clínicos sin modificar su contenido.
    public ConsultaRequest {
        motivoConsulta =
                normalizarTextoObligatorio(
                        motivoConsulta
                );

        anamnesis =
                normalizarTextoOpcional(
                        anamnesis
                );

        examenFisico =
                normalizarTextoOpcional(
                        examenFisico
                );

        evaluacionClinica =
                normalizarTextoOpcional(
                        evaluacionClinica
                );

        planTratamiento =
                normalizarTextoOpcional(
                        planTratamiento
                );

        recomendaciones =
                normalizarTextoOpcional(
                        recomendaciones
                );
    }

    // Limpia el texto obligatorio.
    private static String normalizarTextoObligatorio(
            final String valor
    ) {
        if (valor == null) {
            return null;
        }

        return valor.trim();
    }

    // Convierte los textos clínicos vacíos en null.
    private static String normalizarTextoOpcional(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}