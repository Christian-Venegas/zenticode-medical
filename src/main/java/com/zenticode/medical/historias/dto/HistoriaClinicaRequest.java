package com.zenticode.medical.historias.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos generales y antecedentes de una historia clínica.
 */
public record HistoriaClinicaRequest(

        @Pattern(
                regexp =
                        "^$|"
                                + "^(A|B|AB|O)[+-]$|"
                                + "^(A|B|AB|O)_(POSITIVO|NEGATIVO)$|"
                                + "^DESCONOCIDO$",
                message =
                        "El grupo sanguíneo debe tener "
                                + "un formato válido."
        )
        @Size(
                max = 20,
                message =
                        "El grupo sanguíneo no puede superar "
                                + "los 20 caracteres."
        )
        String grupoSanguineo,

        @Size(
                max = 120,
                message =
                        "La ocupación no puede superar "
                                + "los 120 caracteres."
        )
        String ocupacion,

        @Size(
                max = 50,
                message =
                        "El estado civil no puede superar "
                                + "los 50 caracteres."
        )
        String estadoCivil,

        @Size(
                max = 150,
                message =
                        "El lugar de nacimiento no puede superar "
                                + "los 150 caracteres."
        )
        String lugarNacimiento,

        @Size(
                max = 10000,
                message =
                        "Los antecedentes personales no pueden "
                                + "superar los 10000 caracteres."
        )
        String antecedentesPersonales,

        @Size(
                max = 10000,
                message =
                        "Los antecedentes familiares no pueden "
                                + "superar los 10000 caracteres."
        )
        String antecedentesFamiliares,

        @Size(
                max = 10000,
                message =
                        "Los antecedentes quirúrgicos no pueden "
                                + "superar los 10000 caracteres."
        )
        String antecedentesQuirurgicos,

        @Size(
                max = 10000,
                message =
                        "Los antecedentes farmacológicos no pueden "
                                + "superar los 10000 caracteres."
        )
        String antecedentesFarmacologicos,

        @Size(
                max = 2000,
                message =
                        "Las observaciones generales no pueden "
                                + "superar los 2000 caracteres."
        )
        String observacionesGenerales

) {

    // Normaliza los datos antes de enviarlos al dominio.
    public HistoriaClinicaRequest {
        grupoSanguineo =
                normalizarGrupoSanguineo(
                        grupoSanguineo
                );

        ocupacion =
                normalizarTextoOpcional(
                        ocupacion
                );

        estadoCivil =
                normalizarTextoOpcional(
                        estadoCivil
                );

        lugarNacimiento =
                normalizarTextoOpcional(
                        lugarNacimiento
                );

        antecedentesPersonales =
                normalizarTextoOpcional(
                        antecedentesPersonales
                );

        antecedentesFamiliares =
                normalizarTextoOpcional(
                        antecedentesFamiliares
                );

        antecedentesQuirurgicos =
                normalizarTextoOpcional(
                        antecedentesQuirurgicos
                );

        antecedentesFarmacologicos =
                normalizarTextoOpcional(
                        antecedentesFarmacologicos
                );

        observacionesGenerales =
                normalizarTextoOpcional(
                        observacionesGenerales
                );
    }

    // Convierte formatos visibles al formato persistido.
    private static String normalizarGrupoSanguineo(
            final String valor
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            return "DESCONOCIDO";
        }

        final String grupo =
                valor
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "_");

        return switch (grupo) {
            case "A+", "A_POSITIVO" ->
                    "A_POSITIVO";

            case "A-", "A_NEGATIVO" ->
                    "A_NEGATIVO";

            case "B+", "B_POSITIVO" ->
                    "B_POSITIVO";

            case "B-", "B_NEGATIVO" ->
                    "B_NEGATIVO";

            case "AB+", "AB_POSITIVO" ->
                    "AB_POSITIVO";

            case "AB-", "AB_NEGATIVO" ->
                    "AB_NEGATIVO";

            case "O+", "O_POSITIVO" ->
                    "O_POSITIVO";

            case "O-", "O_NEGATIVO" ->
                    "O_NEGATIVO";

            case "DESCONOCIDO" ->
                    "DESCONOCIDO";

            default ->
                    grupo;
        };
    }

    // Convierte textos opcionales vacíos en null.
    private static String normalizarTextoOpcional(
            final String valor
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            return null;
        }

        return valor.trim();
    }
}