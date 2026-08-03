package com.zenticode.medical.pacientes.dto;

import com.zenticode.medical.pacientes.entity.Paciente.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Datos administrativos para crear o actualizar un paciente.
 */
public record PacienteRequest(

        @NotNull(
                message = "El tipo de documento es obligatorio."
        )
        TipoDocumento tipoDocumento,

        @NotBlank(
                message = "El número de documento es obligatorio."
        )
        @Size(
                min = 4,
                max = 20,
                message = "El número de documento debe contener "
                        + "entre 4 y 20 caracteres."
        )
        @Pattern(
                regexp = "^[A-Za-z0-9 -]+$",
                message = "El número de documento contiene "
                        + "caracteres no permitidos."
        )
        String numeroDocumento,

        @NotBlank(
                message = "Los nombres del paciente son obligatorios."
        )
        @Size(
                min = 2,
                max = 100,
                message = "Los nombres deben contener "
                        + "entre 2 y 100 caracteres."
        )
        String nombres,

        @NotBlank(
                message = "Los apellidos del paciente son obligatorios."
        )
        @Size(
                min = 2,
                max = 100,
                message = "Los apellidos deben contener "
                        + "entre 2 y 100 caracteres."
        )
        String apellidos,

        @PastOrPresent(
                message = "La fecha de nacimiento "
                        + "no puede estar en el futuro."
        )
        LocalDate fechaNacimiento,

        @Size(
                max = 20,
                message = "El teléfono no puede superar "
                        + "los 20 caracteres."
        )
        @Pattern(
                regexp = "^$|^[0-9+() -]{6,20}$",
                message = "El teléfono no tiene un formato válido."
        )
        String telefono,

        @Email(
                message = "El correo no tiene un formato válido."
        )
        @Size(
                max = 180,
                message = "El correo no puede superar "
                        + "los 180 caracteres."
        )
        String correo,

        @Size(
                max = 250,
                message = "La dirección no puede superar "
                        + "los 250 caracteres."
        )
        String direccion,

        @Size(
                max = 150,
                message = "El contacto de emergencia no puede superar "
                        + "los 150 caracteres."
        )
        String contactoEmergencia,

        @Size(
                max = 20,
                message = "El teléfono de emergencia no puede superar "
                        + "los 20 caracteres."
        )
        @Pattern(
                regexp = "^$|^[0-9+() -]{6,20}$",
                message = "El teléfono de emergencia "
                        + "no tiene un formato válido."
        )
        String telefonoEmergencia

) {

    // Normaliza los datos antes de enviarlos al dominio.
    public PacienteRequest {
        numeroDocumento =
                normalizarDocumento(numeroDocumento);

        nombres =
                normalizarTexto(nombres);

        apellidos =
                normalizarTexto(apellidos);

        telefono =
                normalizarTextoOpcional(telefono);

        correo =
                normalizarCorreo(correo);

        direccion =
                normalizarTextoOpcional(direccion);

        contactoEmergencia =
                normalizarTextoOpcional(
                        contactoEmergencia
                );

        telefonoEmergencia =
                normalizarTextoOpcional(
                        telefonoEmergencia
                );
    }

    // Normaliza el documento para evitar duplicados por formato.
    private static String normalizarDocumento(
            final String valor
    ) {
        if (valor == null) {
            return null;
        }

        return valor
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "");
    }

    // Limpia los textos obligatorios.
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

    // Normaliza correos opcionales.
    private static String normalizarCorreo(
            final String valor
    ) {
        final String correo =
                normalizarTextoOpcional(valor);

        if (correo == null) {
            return null;
        }

        return correo.toLowerCase(Locale.ROOT);
    }
}