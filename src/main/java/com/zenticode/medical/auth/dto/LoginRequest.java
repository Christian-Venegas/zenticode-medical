package com.zenticode.medical.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Datos necesarios para iniciar sesión.
 */
public record LoginRequest(

        @NotNull(
                message = "El identificador del consultorio es obligatorio."
        )
        @Positive(
                message = "El identificador del consultorio "
                        + "debe ser mayor que cero."
        )
        Long idConsultorios,

        @NotBlank(
                message = "El correo es obligatorio."
        )
        @Email(
                message = "El correo no tiene un formato válido."
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
        String password

) {

    // Normaliza el correo sin modificar la contraseña.
    public LoginRequest {
        correo = normalizarCorreo(correo);
    }

    // Convierte el correo a minúsculas y elimina espacios laterales.
    private static String normalizarCorreo(
            final String valor
    ) {
        if (valor == null) {
            return null;
        }

        return valor
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}