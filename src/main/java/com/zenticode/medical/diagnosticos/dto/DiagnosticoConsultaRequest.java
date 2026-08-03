package com.zenticode.medical.diagnosticos.dto;

import com.zenticode.medical.diagnosticos.entity
        .DiagnosticoConsulta.TipoDiagnostico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos necesarios para registrar o editar un diagnóstico.
 */
public record DiagnosticoConsultaRequest(

        @Size(
                max = 12,
                message = "El código CIE-10 no puede superar "
                        + "los 12 caracteres."
        )
        @Pattern(
                regexp = "^$|^[A-Za-z][0-9]{2}"
                        + "(\\.[A-Za-z0-9]{1,4})?$",
                message = "El código CIE-10 no tiene "
                        + "un formato válido."
        )
        String codigoCie10,

        @NotBlank(
                message = "La descripción del diagnóstico "
                        + "es obligatoria."
        )
        @Size(
                min = 3,
                max = 500,
                message = "La descripción del diagnóstico "
                        + "debe contener entre 3 y 500 caracteres."
        )
        String descripcion,

        @NotNull(
                message = "El tipo de diagnóstico es obligatorio."
        )
        TipoDiagnostico tipo,

        boolean principal

) {

    // Normaliza la información antes de enviarla al dominio.
    public DiagnosticoConsultaRequest {
        codigoCie10 =
                normalizarCodigoCie10(
                        codigoCie10
                );

        descripcion =
                normalizarDescripcion(
                        descripcion
                );
    }

    // Normaliza un código CIE-10 opcional.
    private static String normalizarCodigoCie10(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "");
    }

    // Limpia la descripción sin alterar el contenido clínico.
    private static String normalizarDescripcion(
            final String valor
    ) {
        if (valor == null) {
            return null;
        }

        return valor.trim();
    }
}