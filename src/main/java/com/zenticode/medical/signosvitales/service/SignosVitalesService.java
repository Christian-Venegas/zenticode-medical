package com.zenticode.medical.signosvitales.service;

import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultas.entity.Consulta.EstadoConsulta;
import com.zenticode.medical.consultas.repository.ConsultaRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import com.zenticode.medical.signosvitales.dto.SignosVitalesRequest;
import com.zenticode.medical.signosvitales.dto.SignosVitalesResponse;
import com.zenticode.medical.signosvitales.entity.SignosVitales;
import com.zenticode.medical.signosvitales.repository.SignosVitalesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Gestiona los signos vitales registrados en consultas médicas.
 */
@Service
public class SignosVitalesService {

    private final SignosVitalesRepository signosVitalesRepository;
    private final ConsultaRepository consultaRepository;

    // Inyecta los repositorios obligatorios.
    public SignosVitalesService(
            final SignosVitalesRepository signosVitalesRepository,
            final ConsultaRepository consultaRepository
    ) {
        this.signosVitalesRepository =
                Objects.requireNonNull(
                        signosVitalesRepository,
                        "El repositorio de signos vitales "
                                + "es obligatorio."
                );

        this.consultaRepository =
                Objects.requireNonNull(
                        consultaRepository,
                        "El repositorio de consultas "
                                + "es obligatorio."
                );
    }

    // Registra signos vitales dentro de una consulta médica.
    @Transactional
    public SignosVitalesResponse registrar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final SignosVitalesRequest solicitud
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        Objects.requireNonNull(
                solicitud,
                "Los signos vitales son obligatorios."
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaDisponibleParaRegistro(
                consulta
        );

        final SignosVitales signosVitales =
                new SignosVitales(
                        consulta.getConsultorio(),
                        consulta,
                        idUsuarioResponsable,
                        solicitud.temperaturaC(),
                        solicitud.presionSistolicaMmhg(),
                        solicitud.presionDiastolicaMmhg(),
                        solicitud.frecuenciaCardiacaLpm(),
                        solicitud.frecuenciaRespiratoriaRpm(),
                        solicitud.saturacionOxigenoPct(),
                        solicitud.pesoKg(),
                        solicitud.tallaCm(),
                        solicitud.perimetroAbdominalCm(),
                        solicitud.observaciones()
                );

        final SignosVitales registroGuardado =
                signosVitalesRepository.saveAndFlush(
                        signosVitales
                );

        return SignosVitalesResponse.desdeEntidad(
                registroGuardado
        );
    }

    // Obtiene un registro específico de signos vitales.
    @Transactional(readOnly = true)
    public SignosVitalesResponse buscarPorId(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idSignosVitales
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

        validarId(
                idSignosVitales,
                "El identificador de signos vitales "
                        + "no es válido."
        );

        // Confirma primero la relación completa.
        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        final SignosVitales signosVitales =
                signosVitalesRepository
                        .findByIdSignosVitalesAndConsultorioIdConsultoriosAndConsultaIdConsultas(
                                idSignosVitales,
                                idConsultorios,
                                idConsultas
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "VITAL_SIGNS_NOT_FOUND",
                                        "El registro de signos vitales "
                                                + "no existe."
                                )
                        );

        return SignosVitalesResponse.desdeEntidad(
                signosVitales
        );
    }

    // Lista las mediciones registradas en una consulta.
    @Transactional(readOnly = true)
    public List<SignosVitalesResponse> listarPorConsulta(
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

        // Impide consultar mediciones de otro paciente.
        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        return signosVitalesRepository
                .findAllByConsultorioIdConsultoriosAndConsultaIdConsultasOrderByFechaRegistroDescIdSignosVitalesDesc(
                        idConsultorios,
                        idConsultas
                )
                .stream()
                .map(SignosVitalesResponse::desdeEntidad)
                .toList();
    }

    // Lista la evolución completa de mediciones del paciente.
    @Transactional(readOnly = true)
    public List<SignosVitalesResponse> listarEvolucionPaciente(
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

        return signosVitalesRepository
                .findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesOrderByFechaRegistroDescIdSignosVitalesDesc(
                        idConsultorios,
                        idPacientes
                )
                .stream()
                .map(SignosVitalesResponse::desdeEntidad)
                .toList();
    }

    // Cuenta las mediciones existentes del paciente.
    @Transactional(readOnly = true)
    public long contarRegistrosPaciente(
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

        return signosVitalesRepository
                .countByConsultorioIdConsultoriosAndConsultaPacienteIdPacientes(
                        idConsultorios,
                        idPacientes
                );
    }

    // Busca la consulta validando consultorio y paciente.
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

    // Impide registrar mediciones en consultas anuladas.
    private static void validarConsultaDisponibleParaRegistro(
            final Consulta consulta
    ) {
        if (consulta.getEstado() == EstadoConsulta.ANULADA) {
            throw new BusinessConflictException(
                    "CANCELLED_CONSULTATION_NOT_EDITABLE",
                    "No pueden registrarse signos vitales "
                            + "en una consulta anulada."
            );
        }

        if (consulta.getEstado() == EstadoConsulta.CERRADA) {
            throw new BusinessConflictException(
                    "CLOSED_CONSULTATION_NOT_EDITABLE",
                    "No pueden registrarse nuevos signos vitales "
                            + "en una consulta cerrada."
            );
        }

        if (consulta.getEstado() != EstadoConsulta.ABIERTA) {
            throw new BusinessConflictException(
                    "CONSULTATION_NOT_AVAILABLE",
                    "La consulta no se encuentra disponible "
                            + "para registrar signos vitales."
            );
        }
    }

    // Valida las claves necesarias para registrar mediciones.
    private static void validarIdentificadores(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
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
                idConsultas,
                "El identificador de la consulta "
                        + "no es válido."
        );

        validarId(
                idUsuarioResponsable,
                "El identificador del usuario responsable "
                        + "no es válido."
        );
    }

    // Comprueba que una clave primaria sea positiva.
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