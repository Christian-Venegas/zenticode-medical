package com.zenticode.medical.consultas.service;

import com.zenticode.medical.consultas.dto.ConsultaRequest;
import com.zenticode.medical.consultas.dto.ConsultaResponse;
import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultas.entity.Consulta.EstadoConsulta;
import com.zenticode.medical.consultas.repository.ConsultaRepository;
import com.zenticode.medical.diagnosticos.entity.DiagnosticoConsulta.EstadoDiagnostico;
import com.zenticode.medical.diagnosticos.repository.DiagnosticoConsultaRepository;
import com.zenticode.medical.historias.entity.HistoriaClinica;
import com.zenticode.medical.historias.entity.HistoriaClinica.EstadoHistoriaClinica;
import com.zenticode.medical.historias.repository.HistoriaClinicaRepository;
import com.zenticode.medical.pacientes.entity.Paciente;
import com.zenticode.medical.pacientes.entity.Paciente.EstadoPaciente;
import com.zenticode.medical.pacientes.repository.PacienteRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Gestiona las atenciones médicas del historial clínico.
 */
@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final DiagnosticoConsultaRepository
            diagnosticoConsultaRepository;

    // Inyecta los repositorios obligatorios.
    public ConsultaService(
            final ConsultaRepository consultaRepository,
            final PacienteRepository pacienteRepository,
            final HistoriaClinicaRepository historiaClinicaRepository,
            final DiagnosticoConsultaRepository
                    diagnosticoConsultaRepository
    ) {
        this.consultaRepository =
                Objects.requireNonNull(
                        consultaRepository,
                        "El repositorio de consultas "
                                + "es obligatorio."
                );

        this.pacienteRepository =
                Objects.requireNonNull(
                        pacienteRepository,
                        "El repositorio de pacientes "
                                + "es obligatorio."
                );

        this.historiaClinicaRepository =
                Objects.requireNonNull(
                        historiaClinicaRepository,
                        "El repositorio de historias clínicas "
                                + "es obligatorio."
                );

        this.diagnosticoConsultaRepository =
                Objects.requireNonNull(
                        diagnosticoConsultaRepository,
                        "El repositorio de diagnósticos "
                                + "es obligatorio."
                );
    }

    // Registra una atención médica.
    @Transactional
    public ConsultaResponse crear(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable,
            final ConsultaRequest solicitud
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos de la consulta son obligatorios."
        );

        final Paciente paciente =
                buscarPacienteActivo(
                        idConsultorios,
                        idPacientes
                );

        final HistoriaClinica historiaClinica =
                buscarHistoriaActiva(
                        idConsultorios,
                        idPacientes
                );

        validarCitaDisponible(
                idConsultorios,
                solicitud.idCitas()
        );

        final Consulta consulta =
                new Consulta(
                        paciente.getConsultorio(),
                        paciente,
                        historiaClinica,
                        idUsuarioResponsable,
                        solicitud.idCitas(),
                        solicitud.motivoConsulta(),
                        solicitud.anamnesis(),
                        solicitud.examenFisico(),
                        solicitud.evaluacionClinica(),
                        solicitud.planTratamiento(),
                        solicitud.recomendaciones()
                );

        final Consulta consultaGuardada =
                consultaRepository.saveAndFlush(
                        consulta
                );

        return ConsultaResponse.desdeEntidad(
                consultaGuardada
        );
    }

    // Obtiene una consulta concreta.
    @Transactional(readOnly = true)
    public ConsultaResponse buscarPorId(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        return ConsultaResponse.desdeEntidad(
                consulta
        );
    }

    // Lista cronológicamente las consultas del paciente.
    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarHistorial(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio "
                        + "no es válido."
        );

        validarId(
                idPacientes,
                "El identificador del paciente "
                        + "no es válido."
        );

        buscarPacienteActivo(
                idConsultorios,
                idPacientes
        );

        return consultaRepository
                .findAllByConsultorioIdConsultoriosAndPacienteIdPacientesOrderByFechaHoraAtencionDescIdConsultasDesc(
                        idConsultorios,
                        idPacientes
                )
                .stream()
                .map(ConsultaResponse::desdeEntidad)
                .toList();
    }

    // Actualiza una consulta que permanece abierta.
    @Transactional
    public ConsultaResponse actualizar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final ConsultaRequest solicitud
    ) {
        validarIdentificadoresConsulta(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos de la consulta son obligatorios."
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaAbierta(consulta);

        validarCitaSinCambio(
                consulta,
                solicitud.idCitas()
        );

        consulta.actualizar(
                idUsuarioResponsable,
                solicitud.motivoConsulta(),
                solicitud.anamnesis(),
                solicitud.examenFisico(),
                solicitud.evaluacionClinica(),
                solicitud.planTratamiento(),
                solicitud.recomendaciones()
        );

        final Consulta consultaActualizada =
                consultaRepository.saveAndFlush(
                        consulta
                );

        return ConsultaResponse.desdeEntidad(
                consultaActualizada
        );
    }

    // Cierra una consulta clínicamente completa.
    @Transactional
    public ConsultaResponse cerrar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable
    ) {
        validarIdentificadoresConsulta(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaAbierta(consulta);

        validarConsultaCompletaParaCierre(
                idConsultorios,
                idConsultas,
                consulta
        );

        consulta.cerrar(
                idUsuarioResponsable
        );

        final Consulta consultaCerrada =
                consultaRepository.saveAndFlush(
                        consulta
                );

        return ConsultaResponse.desdeEntidad(
                consultaCerrada
        );
    }

    // Anula una consulta conservando trazabilidad.
    @Transactional
    public ConsultaResponse anular(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final String motivoAnulacion
    ) {
        validarIdentificadoresConsulta(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        if (consulta.getEstado() == EstadoConsulta.ANULADA) {
            throw new BusinessConflictException(
                    "CONSULTATION_ALREADY_CANCELLED",
                    "La consulta ya se encuentra anulada."
            );
        }

        consulta.anular(
                idUsuarioResponsable,
                motivoAnulacion
        );

        final Consulta consultaAnulada =
                consultaRepository.saveAndFlush(
                        consulta
                );

        return ConsultaResponse.desdeEntidad(
                consultaAnulada
        );
    }

    // Busca un paciente activo dentro del consultorio.
    private Paciente buscarPacienteActivo(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        return pacienteRepository
                .findByIdPacientesAndConsultorioIdConsultoriosAndEstado(
                        idPacientes,
                        idConsultorios,
                        EstadoPaciente.ACTIVO
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "PATIENT_NOT_FOUND",
                                "El paciente solicitado no existe "
                                        + "o no está activo."
                        )
                );
    }

    // Busca la historia clínica activa del paciente.
    private HistoriaClinica buscarHistoriaActiva(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        return historiaClinicaRepository
                .findByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstado(
                        idConsultorios,
                        idPacientes,
                        EstadoHistoriaClinica.ACTIVA
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "CLINICAL_HISTORY_NOT_FOUND",
                                "El paciente no tiene "
                                        + "una historia clínica activa."
                        )
                );
    }

    // Busca una consulta sin permitir cruces SaaS.
    private Consulta buscarConsulta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        return consultaRepository
                .findByIdConsultasAndConsultorioIdConsultoriosAndPacienteIdPacientes(
                        idConsultas,
                        idConsultorios,
                        idPacientes
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "CONSULTATION_NOT_FOUND",
                                "La consulta médica solicitada "
                                        + "no existe."
                        )
                );
    }

    // Verifica que la consulta tenga contenido clínico suficiente.
    private void validarConsultaCompletaParaCierre(
            final Long idConsultorios,
            final Long idConsultas,
            final Consulta consulta
    ) {
        if (consulta.getMotivoConsulta() == null
                || consulta.getMotivoConsulta().isBlank()) {
            throw new BusinessConflictException(
                    "CONSULTATION_REASON_REQUIRED",
                    "La consulta no puede cerrarse "
                            + "sin motivo de consulta."
            );
        }

        if (consulta.getEvaluacionClinica() == null
                || consulta.getEvaluacionClinica().isBlank()) {
            throw new BusinessConflictException(
                    "CLINICAL_ASSESSMENT_REQUIRED",
                    "La consulta no puede cerrarse "
                            + "sin evaluación clínica."
            );
        }

        final long totalDiagnosticosActivos =
                diagnosticoConsultaRepository
                        .countByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstado(
                                idConsultorios,
                                idConsultas,
                                EstadoDiagnostico.ACTIVO
                        );

        if (totalDiagnosticosActivos <= 0) {
            throw new BusinessConflictException(
                    "ACTIVE_DIAGNOSIS_REQUIRED",
                    "La consulta no puede cerrarse "
                            + "sin al menos un diagnóstico activo."
            );
        }

        final boolean tieneDiagnosticoPrincipal =
                diagnosticoConsultaRepository
                        .existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndPrincipalTrueAndEstado(
                                idConsultorios,
                                idConsultas,
                                EstadoDiagnostico.ACTIVO
                        );

        if (!tieneDiagnosticoPrincipal) {
            throw new BusinessConflictException(
                    "MAIN_DIAGNOSIS_REQUIRED",
                    "La consulta no puede cerrarse "
                            + "sin un diagnóstico principal activo."
            );
        }
    }

    // Evita registrar dos consultas para la misma cita.
    private void validarCitaDisponible(
            final Long idConsultorios,
            final Long idCitas
    ) {
        if (idCitas == null) {
            return;
        }

        validarId(
                idCitas,
                "El identificador de la cita no es válido."
        );

        final boolean consultaExistente =
                consultaRepository
                        .existsByConsultorioIdConsultoriosAndIdCitas(
                                idConsultorios,
                                idCitas
                        );

        if (consultaExistente) {
            throw new BusinessConflictException(
                    "APPOINTMENT_ALREADY_HAS_CONSULTATION",
                    "La cita ya se encuentra vinculada "
                            + "a una consulta médica."
            );
        }
    }

    // Impide alterar la cita vinculada.
    private static void validarCitaSinCambio(
            final Consulta consulta,
            final Long idCitasSolicitado
    ) {
        if (!Objects.equals(
                consulta.getIdCitas(),
                idCitasSolicitado
        )) {
            throw new BusinessConflictException(
                    "CONSULTATION_APPOINTMENT_CANNOT_CHANGE",
                    "La cita vinculada a la consulta "
                            + "no puede modificarse."
            );
        }
    }

    // Exige una consulta abierta.
    private static void validarConsultaAbierta(
            final Consulta consulta
    ) {
        if (consulta.getEstado() != EstadoConsulta.ABIERTA) {
            throw new BusinessConflictException(
                    "CONSULTATION_NOT_EDITABLE",
                    "La consulta ya no puede modificarse."
            );
        }
    }

    // Valida la ruta completa de una consulta.
    private static void validarRutaConsulta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio "
                        + "no es válido."
        );

        validarId(
                idPacientes,
                "El identificador del paciente "
                        + "no es válido."
        );

        validarId(
                idConsultas,
                "El identificador de la consulta "
                        + "no es válido."
        );
    }

    // Valida las claves de creación.
    private static void validarIdentificadores(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio "
                        + "no es válido."
        );

        validarId(
                idPacientes,
                "El identificador del paciente "
                        + "no es válido."
        );

        validarId(
                idUsuarioResponsable,
                "El identificador del profesional "
                        + "responsable no es válido."
        );
    }

    // Valida las claves de una consulta existente.
    private static void validarIdentificadoresConsulta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );

        validarId(
                idConsultas,
                "El identificador de la consulta "
                        + "no es válido."
        );
    }

    // Comprueba que una PK sea positiva.
    private static void validarId(
            final Long identificador,
            final String mensaje
    ) {
        if (identificador == null
                || identificador <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}