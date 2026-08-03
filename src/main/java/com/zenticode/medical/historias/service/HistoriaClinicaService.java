package com.zenticode.medical.historias.service;

import com.zenticode.medical.historias.dto.HistoriaClinicaRequest;
import com.zenticode.medical.historias.dto.HistoriaClinicaResponse;
import com.zenticode.medical.historias.entity.HistoriaClinica;
import com.zenticode.medical.historias.entity.HistoriaClinica.EstadoHistoriaClinica;
import com.zenticode.medical.historias.repository.HistoriaClinicaRepository;
import com.zenticode.medical.pacientes.entity.Paciente;
import com.zenticode.medical.pacientes.repository.PacienteRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Gestiona la historia clínica general.
 */
@Service
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository
            historiaClinicaRepository;

    private final PacienteRepository
            pacienteRepository;

    // Inyecta los repositorios obligatorios.
    public HistoriaClinicaService(
            final HistoriaClinicaRepository
                    historiaClinicaRepository,
            final PacienteRepository
                    pacienteRepository
    ) {
        this.historiaClinicaRepository =
                Objects.requireNonNull(
                        historiaClinicaRepository,
                        "El repositorio de historias "
                                + "clínicas es obligatorio."
                );

        this.pacienteRepository =
                Objects.requireNonNull(
                        pacienteRepository,
                        "El repositorio de pacientes "
                                + "es obligatorio."
                );
    }

    // Abre una historia clínica para el paciente.
    @Transactional
    public HistoriaClinicaResponse abrir(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable,
            final HistoriaClinicaRequest solicitud
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );

        final HistoriaClinicaRequest solicitudValidada =
                validarSolicitud(
                        solicitud
                );

        final Paciente paciente =
                buscarPacienteActivo(
                        idConsultorios,
                        idPacientes
                );

        validarHistoriaActivaNoExistente(
                idConsultorios,
                idPacientes
        );

        final String numeroHistoria =
                generarNumeroHistoria();

        final HistoriaClinica historiaClinica =
                new HistoriaClinica(
                        paciente.getConsultorio(),
                        paciente,
                        numeroHistoria,
                        idUsuarioResponsable,
                        solicitudValidada
                                .grupoSanguineo(),
                        solicitudValidada
                                .ocupacion(),
                        solicitudValidada
                                .estadoCivil(),
                        solicitudValidada
                                .lugarNacimiento(),
                        solicitudValidada
                                .antecedentesPersonales(),
                        solicitudValidada
                                .antecedentesFamiliares(),
                        solicitudValidada
                                .antecedentesQuirurgicos(),
                        solicitudValidada
                                .antecedentesFarmacologicos(),
                        solicitudValidada
                                .observacionesGenerales()
                );

        final HistoriaClinica historiaGuardada =
                historiaClinicaRepository
                        .saveAndFlush(
                                historiaClinica
                        );

        return HistoriaClinicaResponse
                .desdeEntidad(
                        historiaGuardada
                );
    }

    // Obtiene la historia clínica activa.
    @Transactional(readOnly = true)
    public HistoriaClinicaResponse buscarActiva(
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

        // Impide acceder mediante otro consultorio.
        buscarPacienteActivo(
                idConsultorios,
                idPacientes
        );

        final HistoriaClinica historiaClinica =
                buscarHistoriaActiva(
                        idConsultorios,
                        idPacientes
                );

        return HistoriaClinicaResponse
                .desdeEntidad(
                        historiaClinica
                );
    }

    // Actualiza el resumen y los antecedentes generales.
    @Transactional
    public HistoriaClinicaResponse actualizar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable,
            final HistoriaClinicaRequest solicitud
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );

        final HistoriaClinicaRequest solicitudValidada =
                validarSolicitud(
                        solicitud
                );

        // Comprueba que el paciente permanezca activo.
        buscarPacienteActivo(
                idConsultorios,
                idPacientes
        );

        final HistoriaClinica historiaClinica =
                buscarHistoriaActiva(
                        idConsultorios,
                        idPacientes
                );

        historiaClinica.actualizar(
                idUsuarioResponsable,
                solicitudValidada
                        .grupoSanguineo(),
                solicitudValidada
                        .ocupacion(),
                solicitudValidada
                        .estadoCivil(),
                solicitudValidada
                        .lugarNacimiento(),
                solicitudValidada
                        .antecedentesPersonales(),
                solicitudValidada
                        .antecedentesFamiliares(),
                solicitudValidada
                        .antecedentesQuirurgicos(),
                solicitudValidada
                        .antecedentesFarmacologicos(),
                solicitudValidada
                        .observacionesGenerales()
        );

        final HistoriaClinica historiaActualizada =
                historiaClinicaRepository
                        .saveAndFlush(
                                historiaClinica
                        );

        return HistoriaClinicaResponse
                .desdeEntidad(
                        historiaActualizada
                );
    }

    // Archiva la historia sin eliminar información.
    @Transactional
    public HistoriaClinicaResponse archivar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );

        // Comprueba que el paciente pertenece al consultorio.
        buscarPacienteActivo(
                idConsultorios,
                idPacientes
        );

        final HistoriaClinica historiaClinica =
                buscarHistoriaActiva(
                        idConsultorios,
                        idPacientes
                );

        historiaClinica.archivar(
                idUsuarioResponsable
        );

        final HistoriaClinica historiaArchivada =
                historiaClinicaRepository
                        .saveAndFlush(
                                historiaClinica
                        );

        return HistoriaClinicaResponse
                .desdeEntidad(
                        historiaArchivada
                );
    }

    // Alias del nuevo contrato para cerrar una historia.
    @Transactional
    public HistoriaClinicaResponse cerrar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable
    ) {
        return archivar(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );
    }

    // Reactiva una historia clínica cerrada.
    @Transactional
    public HistoriaClinicaResponse reabrir(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idUsuarioResponsable
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idUsuarioResponsable
        );

        // Solo se reactiva si el paciente continúa activo.
        buscarPacienteActivo(
                idConsultorios,
                idPacientes
        );

        final HistoriaClinica historiaClinica =
                buscarHistoriaCerrada(
                        idConsultorios,
                        idPacientes
                );

        historiaClinica.reabrir(
                idUsuarioResponsable
        );

        final HistoriaClinica historiaReabierta =
                historiaClinicaRepository
                        .saveAndFlush(
                                historiaClinica
                        );

        return HistoriaClinicaResponse
                .desdeEntidad(
                        historiaReabierta
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
                        Paciente.EstadoPaciente.ACTIVO
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "PATIENT_NOT_FOUND",
                                        "El paciente solicitado no existe "
                                                + "o no está activo."
                                )
                );
    }

    // Obtiene la historia activa sin cruzar consultorios.
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
                        () ->
                                new ResourceNotFoundException(
                                        "CLINICAL_HISTORY_NOT_FOUND",
                                        "El paciente no tiene "
                                                + "una historia clínica activa."
                                )
                );
    }

    // Obtiene la historia cerrada del paciente.
    private HistoriaClinica buscarHistoriaCerrada(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        return historiaClinicaRepository
                .findByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstado(
                        idConsultorios,
                        idPacientes,
                        EstadoHistoriaClinica.CERRADA
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "CLOSED_CLINICAL_HISTORY_NOT_FOUND",
                                        "El paciente no tiene "
                                                + "una historia clínica cerrada."
                                )
                );
    }

    // Evita abrir otra historia activa.
    private void validarHistoriaActivaNoExistente(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        final boolean historiaActivaExistente =
                historiaClinicaRepository
                        .existsByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstado(
                                idConsultorios,
                                idPacientes,
                                EstadoHistoriaClinica.ACTIVA
                        );

        if (historiaActivaExistente) {
            throw new BusinessConflictException(
                    "CLINICAL_HISTORY_ALREADY_EXISTS",
                    "El paciente ya tiene "
                            + "una historia clínica activa."
            );
        }

        final boolean historiaCerradaExistente =
                historiaClinicaRepository
                        .existsByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstado(
                                idConsultorios,
                                idPacientes,
                                EstadoHistoriaClinica.CERRADA
                        );

        if (historiaCerradaExistente) {
            throw new BusinessConflictException(
                    "CLOSED_CLINICAL_HISTORY_ALREADY_EXISTS",
                    "El paciente ya tiene una historia clínica "
                            + "cerrada. Debe reabrirse en lugar "
                            + "de crear una nueva."
            );
        }
    }

    // Valida que la solicitud exista.
    private static HistoriaClinicaRequest validarSolicitud(
            final HistoriaClinicaRequest solicitud
    ) {
        return Objects.requireNonNull(
                solicitud,
                "Los datos de la historia clínica "
                        + "son obligatorios."
        );
    }

    // Genera un número de historia difícil de colisionar.
    private static String generarNumeroHistoria() {
        final String fragmento =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase(Locale.ROOT);

        return "HC-" + fragmento;
    }

    // Valida las claves usadas en operaciones clínicas.
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

    // Comprueba que una clave sea positiva.
    private static void validarId(
            final Long identificador,
            final String mensaje
    ) {
        if (
                identificador == null
                        || identificador <= 0
        ) {
            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }
}