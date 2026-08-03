package com.zenticode.medical.consultas.dto;

import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultas.entity.Consulta.EstadoConsulta;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Información controlada de una consulta médica.
 */
public record ConsultaResponse(

        Long idConsultas,

        Long idConsultorios,

        Long idPacientes,

        Long idHistoriasClinicas,

        Long idUsuariosMedico,

        Long idCitas,

        String nombrePaciente,

        OffsetDateTime fechaHoraAtencion,

        String motivoConsulta,

        String anamnesis,

        String examenFisico,

        String evaluacionClinica,

        String planTratamiento,

        String recomendaciones,

        EstadoConsulta estado,

        boolean editable,

        String motivoAnulacion,

        OffsetDateTime fechaAnulacion,

        Long anuladoPor,

        OffsetDateTime fechaCreacion,

        OffsetDateTime fechaModificacion,

        Long creadoPor,

        Long modificadoPor

) {

    // Convierte la entidad en una respuesta segura.
    public static ConsultaResponse desdeEntidad(
            final Consulta consulta
    ) {
        Objects.requireNonNull(
                consulta,
                "La consulta médica es obligatoria."
        );

        Objects.requireNonNull(
                consulta.getConsultorio(),
                "El consultorio de la consulta es obligatorio."
        );

        Objects.requireNonNull(
                consulta.getPaciente(),
                "El paciente de la consulta es obligatorio."
        );

        Objects.requireNonNull(
                consulta.getHistoriaClinica(),
                "La historia clínica de la consulta "
                        + "es obligatoria."
        );

        final boolean consultaEditable =
                consulta.getEstado() == EstadoConsulta.ABIERTA;

        return new ConsultaResponse(
                consulta.getIdConsultas(),
                consulta
                        .getConsultorio()
                        .getIdConsultorios(),
                consulta
                        .getPaciente()
                        .getIdPacientes(),
                consulta
                        .getHistoriaClinica()
                        .getIdHistoriasClinicas(),
                consulta.getIdUsuariosMedico(),
                consulta.getIdCitas(),
                construirNombrePaciente(consulta),
                consulta.getFechaHoraAtencion(),
                consulta.getMotivoConsulta(),
                consulta.getAnamnesis(),
                consulta.getExamenFisico(),
                consulta.getEvaluacionClinica(),
                consulta.getPlanTratamiento(),
                consulta.getRecomendaciones(),
                consulta.getEstado(),
                consultaEditable,
                consulta.getMotivoAnulacion(),
                consulta.getFechaAnulacion(),
                consulta.getAnuladoPor(),
                consulta.getFechaCreacion(),
                consulta.getFechaModificacion(),
                consulta.getCreadoPor(),
                consulta.getModificadoPor()
        );
    }

    // Construye el nombre completo sin duplicarlo.
    private static String construirNombrePaciente(
            final Consulta consulta
    ) {
        final String nombres =
                consulta
                        .getPaciente()
                        .getNombres();

        final String apellidos =
                consulta
                        .getPaciente()
                        .getApellidos();

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