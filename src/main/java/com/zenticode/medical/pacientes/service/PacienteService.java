package com.zenticode.medical.pacientes.service;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;
import com.zenticode.medical.consultorios.repository.ConsultorioRepository;
import com.zenticode.medical.pacientes.dto.PacienteRequest;
import com.zenticode.medical.pacientes.dto.PacienteResponse;
import com.zenticode.medical.pacientes.entity.Paciente;
import com.zenticode.medical.pacientes.entity.Paciente.EstadoPaciente;
import com.zenticode.medical.pacientes.repository.PacienteRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Gestiona los datos administrativos de pacientes.
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ConsultorioRepository consultorioRepository;

    // Inyecta los repositorios obligatorios.
    public PacienteService(
            final PacienteRepository pacienteRepository,
            final ConsultorioRepository consultorioRepository
    ) {
        this.pacienteRepository = Objects.requireNonNull(
                pacienteRepository,
                "El repositorio de pacientes es obligatorio."
        );

        this.consultorioRepository = Objects.requireNonNull(
                consultorioRepository,
                "El repositorio de consultorios es obligatorio."
        );
    }

    // Crea un paciente dentro del consultorio.
    @Transactional
    public PacienteResponse crear(
            final Long idConsultorios,
            final PacienteRequest solicitud
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos del paciente son obligatorios."
        );

        final Consultorio consultorio =
                buscarConsultorioActivo(idConsultorios);

        validarDocumentoDisponibleAlCrear(
                idConsultorios,
                solicitud
        );

        final Paciente paciente =
                new Paciente(
                        consultorio,
                        solicitud.tipoDocumento(),
                        solicitud.numeroDocumento(),
                        solicitud.nombres(),
                        solicitud.apellidos(),
                        solicitud.fechaNacimiento(),
                        solicitud.telefono(),
                        solicitud.correo(),
                        solicitud.direccion(),
                        solicitud.contactoEmergencia(),
                        solicitud.telefonoEmergencia()
                );

        final Paciente pacienteGuardado =
                pacienteRepository.saveAndFlush(paciente);

        return PacienteResponse.desdeEntidad(
                pacienteGuardado
        );
    }

    // Busca un paciente dentro de su consultorio.
    @Transactional(readOnly = true)
    public PacienteResponse buscarPorId(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        validarId(
                idPacientes,
                "El identificador del paciente no es válido."
        );

        final Paciente paciente =
                buscarPaciente(
                        idConsultorios,
                        idPacientes
                );

        return PacienteResponse.desdeEntidad(paciente);
    }

    // Lista los pacientes activos del consultorio.
    @Transactional(readOnly = true)
    public List<PacienteResponse> listarActivos(
            final Long idConsultorios
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        // Verifica que el consultorio continúe disponible.
        buscarConsultorioActivo(idConsultorios);

        return pacienteRepository
                .findAllByConsultorioIdConsultoriosAndEstadoOrderByApellidosAscNombresAsc(
                        idConsultorios,
                        EstadoPaciente.ACTIVO
                )
                .stream()
                .map(PacienteResponse::desdeEntidad)
                .toList();
    }

    // Busca pacientes activos por nombre, apellido o documento.
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscar(
            final Long idConsultorios,
            final String termino
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        buscarConsultorioActivo(idConsultorios);

        final String terminoNormalizado =
                normalizarTerminoBusqueda(termino);

        if (terminoNormalizado == null) {
            return listarActivos(idConsultorios);
        }

        final List<Paciente> porDocumento =
                pacienteRepository
                        .findAllByConsultorioIdConsultoriosAndEstadoAndNumeroDocumentoContainingIgnoreCaseOrderByApellidosAscNombresAsc(
                                idConsultorios,
                                EstadoPaciente.ACTIVO,
                                terminoNormalizado
                        );

        final List<Paciente> porNombres =
                pacienteRepository
                        .findAllByConsultorioIdConsultoriosAndEstadoAndNombresContainingIgnoreCaseOrderByApellidosAscNombresAsc(
                                idConsultorios,
                                EstadoPaciente.ACTIVO,
                                terminoNormalizado
                        );

        final List<Paciente> porApellidos =
                pacienteRepository
                        .findAllByConsultorioIdConsultoriosAndEstadoAndApellidosContainingIgnoreCaseOrderByApellidosAscNombresAsc(
                                idConsultorios,
                                EstadoPaciente.ACTIVO,
                                terminoNormalizado
                        );

        return unirResultadosSinDuplicados(
                porDocumento,
                porNombres,
                porApellidos
        );
    }

    // Actualiza los datos administrativos del paciente.
    @Transactional
    public PacienteResponse actualizar(
            final Long idConsultorios,
            final Long idPacientes,
            final PacienteRequest solicitud
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        validarId(
                idPacientes,
                "El identificador del paciente no es válido."
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos del paciente son obligatorios."
        );

        final Paciente paciente =
                buscarPaciente(
                        idConsultorios,
                        idPacientes
                );

        validarDocumentoDisponibleAlActualizar(
                idConsultorios,
                idPacientes,
                solicitud
        );

        paciente.actualizar(
                solicitud.tipoDocumento(),
                solicitud.numeroDocumento(),
                solicitud.nombres(),
                solicitud.apellidos(),
                solicitud.fechaNacimiento(),
                solicitud.telefono(),
                solicitud.correo(),
                solicitud.direccion(),
                solicitud.contactoEmergencia(),
                solicitud.telefonoEmergencia()
        );

        final Paciente pacienteActualizado =
                pacienteRepository.saveAndFlush(paciente);

        return PacienteResponse.desdeEntidad(
                pacienteActualizado
        );
    }

    // Desactiva al paciente sin eliminar su información.
    @Transactional
    public PacienteResponse desactivar(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        validarId(
                idConsultorios,
                "El identificador del consultorio no es válido."
        );

        validarId(
                idPacientes,
                "El identificador del paciente no es válido."
        );

        final Paciente paciente =
                buscarPaciente(
                        idConsultorios,
                        idPacientes
                );

        paciente.desactivar();

        final Paciente pacienteActualizado =
                pacienteRepository.saveAndFlush(paciente);

        return PacienteResponse.desdeEntidad(
                pacienteActualizado
        );
    }

    // Busca únicamente consultorios disponibles.
    private Consultorio buscarConsultorioActivo(
            final Long idConsultorios
    ) {
        return consultorioRepository
                .findByIdConsultoriosAndEstado(
                        idConsultorios,
                        EstadoConsultorio.ACTIVO
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "CONSULTORIO_NOT_FOUND",
                                "El consultorio no existe "
                                        + "o no está disponible."
                        )
                );
    }

    // Busca un paciente sin permitir acceso entre consultorios.
    private Paciente buscarPaciente(
            final Long idConsultorios,
            final Long idPacientes
    ) {
        return pacienteRepository
                .findByIdPacientesAndConsultorioIdConsultorios(
                        idPacientes,
                        idConsultorios
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "PATIENT_NOT_FOUND",
                                "El paciente solicitado no existe."
                        )
                );
    }

    // Evita documentos duplicados durante la creación.
    private void validarDocumentoDisponibleAlCrear(
            final Long idConsultorios,
            final PacienteRequest solicitud
    ) {
        final boolean documentoExistente =
                pacienteRepository
                        .existsByConsultorioIdConsultoriosAndTipoDocumentoAndNumeroDocumento(
                                idConsultorios,
                                solicitud.tipoDocumento(),
                                solicitud.numeroDocumento()
                        );

        if (documentoExistente) {
            throw new BusinessConflictException(
                    "PATIENT_DOCUMENT_ALREADY_EXISTS",
                    "Ya existe un paciente con este documento."
            );
        }
    }

    // Evita usar el documento de otro paciente al editar.
    private void validarDocumentoDisponibleAlActualizar(
            final Long idConsultorios,
            final Long idPacientes,
            final PacienteRequest solicitud
    ) {
        final boolean documentoExistente =
                pacienteRepository
                        .existsByConsultorioIdConsultoriosAndTipoDocumentoAndNumeroDocumentoAndIdPacientesNot(
                                idConsultorios,
                                solicitud.tipoDocumento(),
                                solicitud.numeroDocumento(),
                                idPacientes
                        );

        if (documentoExistente) {
            throw new BusinessConflictException(
                    "PATIENT_DOCUMENT_ALREADY_EXISTS",
                    "Ya existe otro paciente con este documento."
            );
        }
    }

    // Une resultados y evita pacientes repetidos.
    private static List<PacienteResponse>
    unirResultadosSinDuplicados(
            final List<Paciente> porDocumento,
            final List<Paciente> porNombres,
            final List<Paciente> porApellidos
    ) {
        return java.util.stream.Stream
                .of(
                        listaSegura(porDocumento),
                        listaSegura(porNombres),
                        listaSegura(porApellidos)
                )
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(
                        java.util.stream.Collectors.toMap(
                                Paciente::getIdPacientes,
                                paciente -> paciente,
                                (pacienteExistente, pacienteRepetido) ->
                                        pacienteExistente,
                                java.util.LinkedHashMap::new
                        )
                )
                .values()
                .stream()
                .sorted(
                        java.util.Comparator
                                .comparing(
                                        Paciente::getApellidos,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .thenComparing(
                                        Paciente::getNombres,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .map(PacienteResponse::desdeEntidad)
                .toList();
    }

    // Evita errores si un repositorio devolviera null.
    private static List<Paciente> listaSegura(
            final List<Paciente> pacientes
    ) {
        return pacientes == null
                ? List.of()
                : pacientes;
    }

    // Limpia y limita el término de búsqueda.
    private static String normalizarTerminoBusqueda(
            final String termino
    ) {
        if (termino == null || termino.isBlank()) {
            return null;
        }

        final String valor =
                termino.trim();

        if (valor.length() > 100) {
            throw new IllegalArgumentException(
                    "El término de búsqueda no puede superar "
                            + "los 100 caracteres."
            );
        }

        return valor.toUpperCase(Locale.ROOT);
    }

    // Comprueba que una clave primaria sea positiva.
    private static void validarId(
            final Long identificador,
            final String mensaje
    ) {
        if (identificador == null || identificador <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}