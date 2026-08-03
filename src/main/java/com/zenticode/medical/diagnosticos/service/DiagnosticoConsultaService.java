package com.zenticode.medical.diagnosticos.service;

import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultas.entity.Consulta.EstadoConsulta;
import com.zenticode.medical.consultas.repository.ConsultaRepository;
import com.zenticode.medical.diagnosticos.dto.DiagnosticoConsultaRequest;
import com.zenticode.medical.diagnosticos.dto.DiagnosticoConsultaResponse;
import com.zenticode.medical.diagnosticos.entity.DiagnosticoConsulta;
import com.zenticode.medical.diagnosticos.entity.DiagnosticoConsulta.EstadoDiagnostico;
import com.zenticode.medical.diagnosticos.repository.DiagnosticoConsultaRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Gestiona los diagnósticos asociados a consultas médicas.
 */
@Service
public class DiagnosticoConsultaService {

    private final DiagnosticoConsultaRepository
            diagnosticoConsultaRepository;

    private final ConsultaRepository consultaRepository;

    // Inyecta los repositorios obligatorios.
    public DiagnosticoConsultaService(
            final DiagnosticoConsultaRepository
                    diagnosticoConsultaRepository,
            final ConsultaRepository consultaRepository
    ) {
        this.diagnosticoConsultaRepository =
                Objects.requireNonNull(
                        diagnosticoConsultaRepository,
                        "El repositorio de diagnósticos "
                                + "es obligatorio."
                );

        this.consultaRepository =
                Objects.requireNonNull(
                        consultaRepository,
                        "El repositorio de consultas "
                                + "es obligatorio."
                );
    }

    // Registra un diagnóstico en una consulta abierta.
    @Transactional
    public DiagnosticoConsultaResponse registrar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final DiagnosticoConsultaRequest solicitud
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos del diagnóstico son obligatorios."
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaAbierta(consulta);

        validarCodigoCie10DisponibleAlCrear(
                idConsultorios,
                idConsultas,
                solicitud.codigoCie10()
        );

        final long totalDiagnosticosActivos =
                diagnosticoConsultaRepository
                        .countByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstado(
                                idConsultorios,
                                idConsultas,
                                EstadoDiagnostico.ACTIVO
                        );

        // El primer diagnóstico siempre será principal.
        final boolean diagnosticoPrincipal =
                solicitud.principal()
                        || totalDiagnosticosActivos == 0;

        if (diagnosticoPrincipal) {
            quitarDiagnosticoPrincipalActual(
                    idConsultorios,
                    idConsultas,
                    idUsuarioResponsable,
                    null
            );
        }

        final DiagnosticoConsulta diagnostico =
                new DiagnosticoConsulta(
                        consulta.getConsultorio(),
                        consulta,
                        idUsuarioResponsable,
                        solicitud.codigoCie10(),
                        solicitud.descripcion(),
                        solicitud.tipo(),
                        diagnosticoPrincipal
                );

        final DiagnosticoConsulta diagnosticoGuardado =
                diagnosticoConsultaRepository.saveAndFlush(
                        diagnostico
                );

        return DiagnosticoConsultaResponse.desdeEntidad(
                diagnosticoGuardado
        );
    }

    // Actualiza un diagnóstico activo de una consulta abierta.
    @Transactional
    public DiagnosticoConsultaResponse actualizar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idDiagnosticosConsultas,
            final Long idUsuarioResponsable,
            final DiagnosticoConsultaRequest solicitud
    ) {
        validarIdentificadoresDiagnostico(
                idConsultorios,
                idPacientes,
                idConsultas,
                idDiagnosticosConsultas,
                idUsuarioResponsable
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos del diagnóstico son obligatorios."
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaAbierta(consulta);

        final DiagnosticoConsulta diagnostico =
                buscarDiagnostico(
                        idConsultorios,
                        idConsultas,
                        idDiagnosticosConsultas
                );

        validarDiagnosticoActivo(diagnostico);

        validarCodigoCie10DisponibleAlActualizar(
                idConsultorios,
                idConsultas,
                idDiagnosticosConsultas,
                solicitud.codigoCie10()
        );

        final boolean eraPrincipal =
                diagnostico.isPrincipal();

        final boolean seraPrincipal =
                solicitud.principal();

        if (eraPrincipal && !seraPrincipal) {
            throw new BusinessConflictException(
                    "MAIN_DIAGNOSIS_REQUIRED",
                    "El diagnóstico principal no puede pasar "
                            + "directamente a secundario. "
                            + "Marque primero otro diagnóstico "
                            + "como principal."
            );
        }

        if (!eraPrincipal && seraPrincipal) {
            quitarDiagnosticoPrincipalActual(
                    idConsultorios,
                    idConsultas,
                    idUsuarioResponsable,
                    idDiagnosticosConsultas
            );
        }

        diagnostico.actualizar(
                idUsuarioResponsable,
                solicitud.codigoCie10(),
                solicitud.descripcion(),
                solicitud.tipo(),
                seraPrincipal
        );

        final DiagnosticoConsulta diagnosticoActualizado =
                diagnosticoConsultaRepository.saveAndFlush(
                        diagnostico
                );

        return DiagnosticoConsultaResponse.desdeEntidad(
                diagnosticoActualizado
        );
    }

    // Desactiva un diagnóstico conservando su auditoría.
    @Transactional
    public DiagnosticoConsultaResponse desactivar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idDiagnosticosConsultas,
            final Long idUsuarioResponsable,
            final String motivoDesactivacion
    ) {
        validarIdentificadoresDiagnostico(
                idConsultorios,
                idPacientes,
                idConsultas,
                idDiagnosticosConsultas,
                idUsuarioResponsable
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaAbierta(consulta);

        final DiagnosticoConsulta diagnostico =
                buscarDiagnostico(
                        idConsultorios,
                        idConsultas,
                        idDiagnosticosConsultas
                );

        validarDiagnosticoActivo(diagnostico);

        final boolean eraPrincipal =
                diagnostico.isPrincipal();

        diagnostico.desactivar(
                idUsuarioResponsable,
                motivoDesactivacion
        );

        // Confirma primero la desactivación y libera el índice único.
        final DiagnosticoConsulta diagnosticoDesactivado =
                diagnosticoConsultaRepository.saveAndFlush(
                        diagnostico
                );

        if (eraPrincipal) {
            asignarNuevoPrincipalSiExiste(
                    idConsultorios,
                    idConsultas,
                    idUsuarioResponsable,
                    idDiagnosticosConsultas
            );
        }

        return DiagnosticoConsultaResponse.desdeEntidad(
                diagnosticoDesactivado
        );
    }

    // Obtiene un diagnóstico concreto.
    @Transactional(readOnly = true)
    public DiagnosticoConsultaResponse buscarPorId(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idDiagnosticosConsultas
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
                idDiagnosticosConsultas,
                "El identificador del diagnóstico "
                        + "no es válido."
        );

        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        final DiagnosticoConsulta diagnostico =
                buscarDiagnostico(
                        idConsultorios,
                        idConsultas,
                        idDiagnosticosConsultas
                );

        return DiagnosticoConsultaResponse.desdeEntidad(
                diagnostico
        );
    }

    // Lista los diagnósticos activos de una consulta.
    @Transactional(readOnly = true)
    public List<DiagnosticoConsultaResponse> listarActivos(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        return diagnosticoConsultaRepository
                .findAllByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstadoOrderByPrincipalDescFechaRegistroAscIdDiagnosticosConsultasAsc(
                        idConsultorios,
                        idConsultas,
                        EstadoDiagnostico.ACTIVO
                )
                .stream()
                .map(
                        DiagnosticoConsultaResponse::desdeEntidad
                )
                .toList();
    }

    // Lista diagnósticos activos e inactivos.
    @Transactional(readOnly = true)
    public List<DiagnosticoConsultaResponse> listarTodos(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        return diagnosticoConsultaRepository
                .findAllByConsultorioIdConsultoriosAndConsultaIdConsultasOrderByPrincipalDescFechaRegistroAscIdDiagnosticosConsultasAsc(
                        idConsultorios,
                        idConsultas
                )
                .stream()
                .map(
                        DiagnosticoConsultaResponse::desdeEntidad
                )
                .toList();
    }

    // Obtiene el diagnóstico principal activo.
    @Transactional(readOnly = true)
    public DiagnosticoConsultaResponse buscarPrincipal(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        final DiagnosticoConsulta diagnosticoPrincipal =
                diagnosticoConsultaRepository
                        .findFirstByConsultorioIdConsultoriosAndConsultaIdConsultasAndPrincipalTrueAndEstado(
                                idConsultorios,
                                idConsultas,
                                EstadoDiagnostico.ACTIVO
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "MAIN_DIAGNOSIS_NOT_FOUND",
                                        "La consulta no tiene un "
                                                + "diagnóstico principal activo."
                                )
                        );

        return DiagnosticoConsultaResponse.desdeEntidad(
                diagnosticoPrincipal
        );
    }

    // Lista la evolución diagnóstica activa del paciente.
    @Transactional(readOnly = true)
    public List<DiagnosticoConsultaResponse>
    listarEvolucionPaciente(
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

        return diagnosticoConsultaRepository
                .findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesAndEstadoOrderByFechaRegistroDescIdDiagnosticosConsultasDesc(
                        idConsultorios,
                        idPacientes,
                        EstadoDiagnostico.ACTIVO
                )
                .stream()
                .map(
                        DiagnosticoConsultaResponse::desdeEntidad
                )
                .toList();
    }

    // Cuenta diagnósticos activos de una consulta.
    @Transactional(readOnly = true)
    public long contarActivos(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        return diagnosticoConsultaRepository
                .countByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstado(
                        idConsultorios,
                        idConsultas,
                        EstadoDiagnostico.ACTIVO
                );
    }

    // Busca la consulta validando paciente y consultorio.
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

    // Busca el diagnóstico sin cruzar consultorios.
    private DiagnosticoConsulta buscarDiagnostico(
            final Long idConsultorios,
            final Long idConsultas,
            final Long idDiagnosticosConsultas
    ) {
        return diagnosticoConsultaRepository
                .findByIdDiagnosticosConsultasAndConsultorioIdConsultoriosAndConsultaIdConsultas(
                        idDiagnosticosConsultas,
                        idConsultorios,
                        idConsultas
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "DIAGNOSIS_NOT_FOUND",
                                "El diagnóstico solicitado "
                                        + "no existe."
                        )
                );
    }

    // Bloquea modificaciones en consultas finalizadas.
    private static void validarConsultaAbierta(
            final Consulta consulta
    ) {
        if (consulta.getEstado() == EstadoConsulta.CERRADA) {
            throw new BusinessConflictException(
                    "CLOSED_CONSULTATION_NOT_EDITABLE",
                    "No pueden modificarse diagnósticos "
                            + "en una consulta cerrada."
            );
        }

        if (consulta.getEstado() == EstadoConsulta.ANULADA) {
            throw new BusinessConflictException(
                    "CANCELLED_CONSULTATION_NOT_EDITABLE",
                    "No pueden modificarse diagnósticos "
                            + "en una consulta anulada."
            );
        }

        if (consulta.getEstado() != EstadoConsulta.ABIERTA) {
            throw new BusinessConflictException(
                    "CONSULTATION_NOT_AVAILABLE",
                    "La consulta no está disponible "
                            + "para modificar diagnósticos."
            );
        }
    }

    // Bloquea operaciones sobre diagnósticos inactivos.
    private static void validarDiagnosticoActivo(
            final DiagnosticoConsulta diagnostico
    ) {
        if (diagnostico.getEstado()
                != EstadoDiagnostico.ACTIVO) {
            throw new BusinessConflictException(
                    "DIAGNOSIS_NOT_EDITABLE",
                    "El diagnóstico ya no se encuentra activo."
            );
        }
    }

    // Evita CIE-10 duplicados al crear.
    private void validarCodigoCie10DisponibleAlCrear(
            final Long idConsultorios,
            final Long idConsultas,
            final String codigoCie10
    ) {
        if (codigoCie10 == null
                || codigoCie10.isBlank()) {
            return;
        }

        final boolean codigoExistente =
                diagnosticoConsultaRepository
                        .existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndCodigoCie10AndEstado(
                                idConsultorios,
                                idConsultas,
                                codigoCie10,
                                EstadoDiagnostico.ACTIVO
                        );

        if (codigoExistente) {
            throw new BusinessConflictException(
                    "DIAGNOSIS_CODE_ALREADY_EXISTS",
                    "Ya existe un diagnóstico activo "
                            + "con este código CIE-10 "
                            + "en la consulta."
            );
        }
    }

    // Evita CIE-10 duplicados excluyendo el diagnóstico editado.
    private void validarCodigoCie10DisponibleAlActualizar(
            final Long idConsultorios,
            final Long idConsultas,
            final Long idDiagnosticosConsultas,
            final String codigoCie10
    ) {
        if (codigoCie10 == null
                || codigoCie10.isBlank()) {
            return;
        }

        final boolean codigoExistente =
                diagnosticoConsultaRepository
                        .existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndCodigoCie10AndEstadoAndIdDiagnosticosConsultasNot(
                                idConsultorios,
                                idConsultas,
                                codigoCie10,
                                EstadoDiagnostico.ACTIVO,
                                idDiagnosticosConsultas
                        );

        if (codigoExistente) {
            throw new BusinessConflictException(
                    "DIAGNOSIS_CODE_ALREADY_EXISTS",
                    "Ya existe otro diagnóstico activo "
                            + "con este código CIE-10 "
                            + "en la consulta."
            );
        }
    }

    // Retira el principal actual antes de asignar otro.
    private void quitarDiagnosticoPrincipalActual(
            final Long idConsultorios,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final Long idDiagnosticoExcluido
    ) {
        diagnosticoConsultaRepository
                .findFirstByConsultorioIdConsultoriosAndConsultaIdConsultasAndPrincipalTrueAndEstado(
                        idConsultorios,
                        idConsultas,
                        EstadoDiagnostico.ACTIVO
                )
                .filter(
                        diagnosticoPrincipal ->
                                idDiagnosticoExcluido == null
                                        || !Objects.equals(
                                        diagnosticoPrincipal
                                                .getIdDiagnosticosConsultas(),
                                        idDiagnosticoExcluido
                                )
                )
                .ifPresent(
                        diagnosticoPrincipal -> {
                            diagnosticoPrincipal
                                    .quitarComoPrincipal(
                                            idUsuarioResponsable
                                    );

                            // Confirma el cambio antes de asignar otro.
                            diagnosticoConsultaRepository.saveAndFlush(
                                    diagnosticoPrincipal
                            );
                        }
                );
    }

    // Asigna otro principal después de desactivar el anterior.
    private void asignarNuevoPrincipalSiExiste(
            final Long idConsultorios,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final Long idDiagnosticoDesactivado
    ) {
        diagnosticoConsultaRepository
                .findAllByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstadoOrderByPrincipalDescFechaRegistroAscIdDiagnosticosConsultasAsc(
                        idConsultorios,
                        idConsultas,
                        EstadoDiagnostico.ACTIVO
                )
                .stream()
                .filter(
                        diagnostico ->
                                !Objects.equals(
                                        diagnostico
                                                .getIdDiagnosticosConsultas(),
                                        idDiagnosticoDesactivado
                                )
                )
                .findFirst()
                .ifPresent(
                        nuevoPrincipal -> {
                            nuevoPrincipal.marcarComoPrincipal(
                                    idUsuarioResponsable
                            );

                            diagnosticoConsultaRepository.saveAndFlush(
                                    nuevoPrincipal
                            );
                        }
                );
    }

    // Valida los identificadores de la ruta.
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

    // Valida las claves necesarias para registrar.
    private static void validarIdentificadores(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        validarId(
                idUsuarioResponsable,
                "El identificador del profesional "
                        + "responsable no es válido."
        );
    }

    // Valida las claves de un diagnóstico existente.
    private static void validarIdentificadoresDiagnostico(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idDiagnosticosConsultas,
            final Long idUsuarioResponsable
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        validarId(
                idDiagnosticosConsultas,
                "El identificador del diagnóstico "
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