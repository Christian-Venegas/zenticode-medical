package com.zenticode.medical.pacientes.dto;

import com.zenticode.medical.pacientes.entity.Paciente;
import com.zenticode.medical.pacientes.entity.Paciente.EstadoPaciente;
import com.zenticode.medical.pacientes.entity.Paciente.TipoDocumento;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Información administrativa pública de un paciente.
 */
public record PacienteResponse(

        Long idPacientes,

        Long idConsultorios,

        TipoDocumento tipoDocumento,

        String numeroDocumento,

        String nombres,

        String apellidos,

        LocalDate fechaNacimiento,

        String telefono,

        String correo,

        String direccion,

        String contactoEmergencia,

        String telefonoEmergencia,

        EstadoPaciente estado,

        OffsetDateTime fechaCreacion,

        OffsetDateTime fechaModificacion

) {

    // Convierte la entidad en una respuesta segura.
    public static PacienteResponse desdeEntidad(
            final Paciente paciente
    ) {
        Objects.requireNonNull(
                paciente,
                "El paciente es obligatorio."
        );

        Objects.requireNonNull(
                paciente.getConsultorio(),
                "El consultorio del paciente es obligatorio."
        );

        return new PacienteResponse(
                paciente.getIdPacientes(),
                paciente
                        .getConsultorio()
                        .getIdConsultorios(),
                paciente.getTipoDocumento(),
                paciente.getNumeroDocumento(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.getFechaNacimiento(),
                paciente.getTelefono(),
                paciente.getCorreo(),
                paciente.getDireccion(),
                paciente.getContactoEmergencia(),
                paciente.getTelefonoEmergencia(),
                paciente.getEstado(),
                paciente.getFechaCreacion(),
                paciente.getFechaModificacion()
        );
    }

    // Devuelve el nombre completo para la interfaz.
    public String nombreCompleto() {
        final String nombresSeguros =
                nombres == null
                        ? ""
                        : nombres.trim();

        final String apellidosSeguros =
                apellidos == null
                        ? ""
                        : apellidos.trim();

        return (
                nombresSeguros
                        + " "
                        + apellidosSeguros
        ).trim();
    }
}