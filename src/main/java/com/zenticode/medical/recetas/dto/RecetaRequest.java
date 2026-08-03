package com.zenticode.medical.recetas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Datos necesarios para emitir una receta médica completa.
 */
public record RecetaRequest(

        @Size(
                max = 10000,
                message = "Las indicaciones generales no pueden "
                        + "superar los 10000 caracteres."
        )
        String indicacionesGenerales,

        @NotEmpty(
                message = "La receta debe contener "
                        + "al menos un medicamento."
        )
        @Size(
                max = 50,
                message = "La receta no puede contener "
                        + "más de 50 medicamentos."
        )
        List<@Valid MedicamentoRequest> medicamentos

) {

    // Normaliza y protege la lista recibida.
    public RecetaRequest {
        indicacionesGenerales =
                normalizarTextoOpcional(
                        indicacionesGenerales
                );

        medicamentos =
                medicamentos == null
                        ? null
                        : List.copyOf(medicamentos);
    }

    // Convierte textos opcionales vacíos en null.
    private static String normalizarTextoOpcional(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    /**
     * Medicamento incluido dentro de la receta.
     */
    public record MedicamentoRequest(

            @NotBlank(
                    message = "El nombre del medicamento "
                            + "es obligatorio."
            )
            @Size(
                    max = 200,
                    message = "El medicamento no puede superar "
                            + "los 200 caracteres."
            )
            String medicamento,

            @Size(
                    max = 120,
                    message = "La presentación no puede superar "
                            + "los 120 caracteres."
            )
            String presentacion,

            @NotBlank(
                    message = "La dosis es obligatoria."
            )
            @Size(
                    max = 120,
                    message = "La dosis no puede superar "
                            + "los 120 caracteres."
            )
            String dosis,

            @Size(
                    max = 80,
                    message = "La vía de administración no puede "
                            + "superar los 80 caracteres."
            )
            String viaAdministracion,

            @NotBlank(
                    message = "La frecuencia es obligatoria."
            )
            @Size(
                    max = 120,
                    message = "La frecuencia no puede superar "
                            + "los 120 caracteres."
            )
            String frecuencia,

            @NotBlank(
                    message = "La duración es obligatoria."
            )
            @Size(
                    max = 120,
                    message = "La duración no puede superar "
                            + "los 120 caracteres."
            )
            String duracion,

            @Size(
                    max = 500,
                    message = "Las indicaciones del medicamento "
                            + "no pueden superar los 500 caracteres."
            )
            String indicaciones

    ) {

        // Normaliza los textos sin cambiar su contenido clínico.
        public MedicamentoRequest {
            medicamento =
                    normalizarTextoObligatorio(
                            medicamento
                    );

            presentacion =
                    normalizarTextoOpcional(
                            presentacion
                    );

            dosis =
                    normalizarTextoObligatorio(
                            dosis
                    );

            viaAdministracion =
                    normalizarTextoOpcional(
                            viaAdministracion
                    );

            frecuencia =
                    normalizarTextoObligatorio(
                            frecuencia
                    );

            duracion =
                    normalizarTextoObligatorio(
                            duracion
                    );

            indicaciones =
                    normalizarTextoOpcional(
                            indicaciones
                    );
        }

        // Elimina espacios laterales de campos obligatorios.
        private static String normalizarTextoObligatorio(
                final String valor
        ) {
            if (valor == null) {
                return null;
            }

            return valor.trim();
        }

        // Convierte textos opcionales vacíos en null.
        private static String normalizarTextoOpcional(
                final String valor
        ) {
            if (valor == null || valor.isBlank()) {
                return null;
            }

            return valor.trim();
        }
    }
}