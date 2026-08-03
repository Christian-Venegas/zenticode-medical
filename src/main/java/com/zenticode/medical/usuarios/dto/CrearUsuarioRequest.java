package com.zenticode.medical.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos necesarios para registrar un usuario.
 */
public record CrearUsuarioRequest(

        @NotBlank(
                message = "El correo del usuario es obligatorio."
        )
        @Email(
                message = "El correo del usuario no tiene un formato válido."
        )
        @Size(
                max = 180,
                message = "El correo no puede superar los 180 caracteres."
        )
        String correo,

        @NotBlank(
                message = "La contraseña es obligatoria."
        )
        @Size(
                min = 10,
                max = 72,
                message = "La contraseña debe contener entre "
                        + "10 y 72 caracteres."
        )
        String password,

        @NotBlank(
                message = "Los nombres del usuario son obligatorios."
        )
        @Size(
                max = 120,
                message = "Los nombres no pueden superar "
                        + "los 120 caracteres."
        )
        String nombres,

        @NotBlank(
                message = "Los apellidos del usuario son obligatorios."
        )
        @Size(
                max = 120,
                message = "Los apellidos no pueden superar "
                        + "los 120 caracteres."
        )
        String apellidos,

        @Size(
                max = 40,
                message = "El número de colegiatura no puede superar "
                        + "los 40 caracteres."
        )
        @Pattern(
                regexp = "^[A-Za-z0-9\\-./]*$",
                message = "El número de colegiatura contiene "
                        + "caracteres no permitidos."
        )
        String numeroColegiatura,

        @Size(
                max = 20,
                message = "El teléfono no puede superar "
                        + "los 20 caracteres."
        )
        @Pattern(
                regexp = "^[0-9+()\\-\\s]*$",
                message = "El teléfono contiene caracteres no permitidos."
        )
        String telefono

) {

    // Normaliza los datos al crear el DTO.
    public CrearUsuarioRequest {
        correo = normalizarCorreo(correo);
        nombres = normalizarTexto(nombres);
        apellidos = normalizarTexto(apellidos);
        numeroColegiatura =
                normalizarTextoOpcional(numeroColegiatura);
        telefono = normalizarTextoOpcional(telefono);

        /*
         * La contraseña no se recorta con trim().
         *
         * Los espacios podrían formar parte intencionalmente de una frase
         * de contraseña. Modificarla aquí haría que el hash no represente
         * exactamente el valor escrito por la persona.
         */
    }

    // Normaliza el correo a minúsculas.
    private static String normalizarCorreo(
            final String correo
    ) {
        final String correoNormalizado =
                normalizarTexto(correo);

        if (correoNormalizado == null) {
            return null;
        }

        return correoNormalizado.toLowerCase(Locale.ROOT);
    }

    // Elimina espacios laterales de los textos obligatorios.
    private static String normalizarTexto(
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