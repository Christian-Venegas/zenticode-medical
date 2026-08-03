package com.zenticode.medical.diagnosticos.dto;

import com.zenticode.medical.diagnosticos.entity.DiagnosticoConsulta;
import com.zenticode.medical.diagnosticos.entity
        .DiagnosticoConsulta.EstadoDiagnostico;
import com.zenticode.medical.diagnosticos.entity
        .DiagnosticoConsulta.TipoDiagnostico;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Información controlada de un diagnóstico médico.
 */
public record DiagnosticoConsultaResponse(

        Long idDiagnosticosConsultas,

        Long idConsultorios,

        Long idPacientes,

        Long idConsultas,

        String codigoCie10,

        String descripcion,

        TipoDiagnostico tipo,

        boolean principal,

        EstadoDiagnostico estado,

        boolean editable,

        OffsetDateTime fechaRegistro,

        Long registradoPor,

        OffsetDateTime fechaModificacion,

        Long modificadoPor,

        String motivoDesactivacion,

        OffsetDateTime fechaDesactivacion,

        Long desactivadoPor

) {

    // Convierte la entidad en una respuesta segura.
    public static DiagnosticoConsultaResponse desdeEntidad(
            final DiagnosticoConsulta diagnostico
    ) {
        Objects.requireNonNull(
                diagnostico,
                "El diagnóstico es obligatorio."
        );

        Objects.requireNonNull(
                diagnostico.getConsultorio(),
                "El consultorio del diagnóstico es obligatorio."
        );

        Objects.requireNonNull(
                diagnostico.getConsulta(),
                "La consulta del diagnóstico es obligatoria."
        );

        Objects.requireNonNull(
                diagnostico
                        .getConsulta()
                        .getPaciente(),
                "El paciente del diagnóstico es obligatorio."
        );

        final boolean diagnosticoEditable =
                diagnostico.getEstado()
                        == EstadoDiagnostico.ACTIVO
                        && diagnostico
                        .getConsulta()
                        .getEstado()
                        == com.zenticode.medical.consultas
                        .entity.Consulta.EstadoConsulta.ABIERTA;

        return new DiagnosticoConsultaResponse(
                diagnostico.getIdDiagnosticosConsultas(),
                diagnostico
                        .getConsultorio()
                        .getIdConsultorios(),
                diagnostico
                        .getConsulta()
                        .getPaciente()
                        .getIdPacientes(),
                diagnostico
                        .getConsulta()
                        .getIdConsultas(),
                diagnostico.getCodigoCie10(),
                diagnostico.getDescripcion(),
                diagnostico.getTipo(),
                diagnostico.isPrincipal(),
                diagnostico.getEstado(),
                diagnosticoEditable,
                diagnostico.getFechaRegistro(),
                diagnostico.getRegistradoPor(),
                diagnostico.getFechaModificacion(),
                diagnostico.getModificadoPor(),
                diagnostico.getMotivoDesactivacion(),
                diagnostico.getFechaDesactivacion(),
                diagnostico.getDesactivadoPor()
        );
    }
}