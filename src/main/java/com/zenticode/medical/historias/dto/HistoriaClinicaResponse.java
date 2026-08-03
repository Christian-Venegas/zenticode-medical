package com.zenticode.medical.historias.dto;

import com.zenticode.medical.historias.entity.HistoriaClinica;
import com.zenticode.medical.historias.entity.HistoriaClinica.EstadoHistoriaClinica;
import com.zenticode.medical.pacientes.entity.Paciente;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Información general de la historia clínica.
 */
public record HistoriaClinicaResponse(

        Long idHistoriasClinicas,

        Long idConsultorios,

        Long idPacientes,

        String numeroHistoria,

        String tipoDocumento,

        String numeroDocumento,

        String nombres,

        String apellidos,

        LocalDate fechaNacimiento,

        String telefono,

        String estadoPaciente,

        String nombrePaciente,

        String grupoSanguineo,

        String ocupacion,

        String estadoCivil,

        String lugarNacimiento,

        String antecedentesPersonales,

        String antecedentesFamiliares,

        String antecedentesQuirurgicos,

        String antecedentesFarmacologicos,

        String observacionesGenerales,

        EstadoHistoriaClinica estado,

        OffsetDateTime fechaApertura,

        OffsetDateTime fechaModificacion,

        Long creadoPor,

        Long modificadoPor

) {

    /**
     * Valida la respuesta antes de exponerla.
     */
    public HistoriaClinicaResponse {
        Objects.requireNonNull(
                idHistoriasClinicas,
                "El identificador de la historia es obligatorio."
        );

        Objects.requireNonNull(
                idConsultorios,
                "El identificador del consultorio es obligatorio."
        );

        Objects.requireNonNull(
                idPacientes,
                "El identificador del paciente es obligatorio."
        );

        Objects.requireNonNull(
                numeroHistoria,
                "El número de historia es obligatorio."
        );

        Objects.requireNonNull(
                tipoDocumento,
                "El tipo de documento es obligatorio."
        );

        Objects.requireNonNull(
                numeroDocumento,
                "El número de documento es obligatorio."
        );

        Objects.requireNonNull(
                nombres,
                "Los nombres del paciente son obligatorios."
        );

        Objects.requireNonNull(
                apellidos,
                "Los apellidos del paciente son obligatorios."
        );

        Objects.requireNonNull(
                estadoPaciente,
                "El estado del paciente es obligatorio."
        );

        Objects.requireNonNull(
                nombrePaciente,
                "El nombre del paciente es obligatorio."
        );

        Objects.requireNonNull(
                grupoSanguineo,
                "El grupo sanguíneo es obligatorio."
        );

        Objects.requireNonNull(
                estado,
                "El estado de la historia es obligatorio."
        );

        Objects.requireNonNull(
                fechaApertura,
                "La fecha de apertura es obligatoria."
        );

        Objects.requireNonNull(
                fechaModificacion,
                "La fecha de modificación es obligatoria."
        );

        Objects.requireNonNull(
                creadoPor,
                "El usuario creador es obligatorio."
        );

        Objects.requireNonNull(
                modificadoPor,
                "El usuario modificador es obligatorio."
        );
    }

    // Convierte la entidad en una respuesta controlada.
    public static HistoriaClinicaResponse desdeEntidad(
            final HistoriaClinica historiaClinica
    ) {
        Objects.requireNonNull(
                historiaClinica,
                "La historia clínica es obligatoria."
        );

        Objects.requireNonNull(
                historiaClinica.getConsultorio(),
                "El consultorio de la historia es obligatorio."
        );

        final Paciente paciente =
                Objects.requireNonNull(
                        historiaClinica.getPaciente(),
                        "El paciente de la historia es obligatorio."
                );

        return new HistoriaClinicaResponse(
                historiaClinica.getIdHistoriasClinicas(),

                historiaClinica
                        .getConsultorio()
                        .getIdConsultorios(),

                paciente.getIdPacientes(),

                historiaClinica.getNumeroHistoria(),

                paciente
                        .getTipoDocumento()
                        .name(),

                paciente.getNumeroDocumento(),

                normalizarTextoObligatorio(
                        paciente.getNombres(),
                        "Los nombres del paciente son obligatorios."
                ),

                normalizarTextoObligatorio(
                        paciente.getApellidos(),
                        "Los apellidos del paciente son obligatorios."
                ),

                paciente.getFechaNacimiento(),

                normalizarTextoOpcional(
                        paciente.getTelefono()
                ),

                paciente
                        .getEstado()
                        .name(),

                construirNombrePaciente(
                        paciente
                ),

                historiaClinica.getGrupoSanguineo(),

                normalizarTextoOpcional(
                        historiaClinica.getOcupacion()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getEstadoCivil()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getLugarNacimiento()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getAntecedentesPersonales()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getAntecedentesFamiliares()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getAntecedentesQuirurgicos()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getAntecedentesFarmacologicos()
                ),

                normalizarTextoOpcional(
                        historiaClinica.getObservacionesGenerales()
                ),

                historiaClinica.getEstado(),

                historiaClinica.getFechaApertura(),

                historiaClinica.getFechaModificacion(),

                historiaClinica.getCreadoPor(),

                historiaClinica.getModificadoPor()
        );
    }

    // Construye el nombre sin duplicarlo en PostgreSQL.
    private static String construirNombrePaciente(
            final Paciente paciente
    ) {
        final String nombres =
                normalizarTextoObligatorio(
                        paciente.getNombres(),
                        "Los nombres del paciente son obligatorios."
                );

        final String apellidos =
                normalizarTextoObligatorio(
                        paciente.getApellidos(),
                        "Los apellidos del paciente son obligatorios."
                );

        return nombres
                + " "
                + apellidos;
    }

    // Normaliza un texto obligatorio.
    private static String normalizarTextoObligatorio(
            final String valor,
            final String mensaje
    ) {
        if (
                valor == null
                        || valor.isBlank()
        ) {
            throw new IllegalStateException(
                    mensaje
            );
        }

        return valor.trim();
    }

    // Normaliza un texto opcional.
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