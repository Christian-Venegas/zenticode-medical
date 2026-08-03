package com.zenticode.medical.recetas.service;

import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultas.entity.Consulta.EstadoConsulta;
import com.zenticode.medical.consultas.repository.ConsultaRepository;
import com.zenticode.medical.recetas.dto.RecetaRequest;
import com.zenticode.medical.recetas.dto.RecetaRequest.MedicamentoRequest;
import com.zenticode.medical.recetas.dto.RecetaResponse;
import com.zenticode.medical.recetas.entity.Receta;
import com.zenticode.medical.recetas.entity.Receta.EstadoReceta;
import com.zenticode.medical.recetas.entity.RecetaDetalle;
import com.zenticode.medical.recetas.repository.RecetaDetalleRepository;
import com.zenticode.medical.recetas.repository.RecetaRepository;
import com.zenticode.medical.shared.exception.BusinessConflictException;
import com.zenticode.medical.shared.exception.ResourceNotFoundException;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Gestiona la emisión y consulta de recetas médicas.
 */
@Service
public class RecetaService {

    private static final int MAXIMO_MEDICAMENTOS = 50;

    private final RecetaRepository recetaRepository;

    private final RecetaDetalleRepository
            recetaDetalleRepository;

    private final ConsultaRepository consultaRepository;

    private final UsuarioRepository usuarioRepository;

    // Inyecta los repositorios obligatorios.
    public RecetaService(
            final RecetaRepository recetaRepository,
            final RecetaDetalleRepository
                    recetaDetalleRepository,
            final ConsultaRepository consultaRepository,
            final UsuarioRepository usuarioRepository
    ) {
        this.recetaRepository =
                Objects.requireNonNull(
                        recetaRepository,
                        "El repositorio de recetas "
                                + "es obligatorio."
                );

        this.recetaDetalleRepository =
                Objects.requireNonNull(
                        recetaDetalleRepository,
                        "El repositorio de detalles de receta "
                                + "es obligatorio."
                );

        this.consultaRepository =
                Objects.requireNonNull(
                        consultaRepository,
                        "El repositorio de consultas "
                                + "es obligatorio."
                );

        this.usuarioRepository =
                Objects.requireNonNull(
                        usuarioRepository,
                        "El repositorio de usuarios "
                                + "es obligatorio."
                );
    }

    // Emite una receta completa dentro de una transacción.
    @Transactional
    public RecetaResponse emitir(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idUsuarioResponsable,
            final RecetaRequest solicitud
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        Objects.requireNonNull(
                solicitud,
                "Los datos de la receta son obligatorios."
        );

        validarMedicamentos(
                solicitud.medicamentos()
        );

        final Consulta consulta =
                buscarConsulta(
                        idConsultorios,
                        idPacientes,
                        idConsultas
                );

        validarConsultaDisponible(
                consulta
        );

        // Confirma que el emisor pertenece al consultorio.
        buscarProfesional(
                idUsuarioResponsable,
                idConsultorios
        );

        final Receta receta =
                new Receta(
                        consulta.getConsultorio(),
                        consulta,
                        idUsuarioResponsable,
                        solicitud.indicacionesGenerales()
                );

        // Guarda la cabecera para obtener idRecetas.
        final Receta recetaGuardada =
                recetaRepository.saveAndFlush(
                        receta
                );

        final List<RecetaDetalle> detalles =
                construirDetalles(
                        recetaGuardada,
                        solicitud.medicamentos()
                );

        final List<RecetaDetalle> detallesGuardados =
                recetaDetalleRepository.saveAll(
                        detalles
                );

        // Fuerza los INSERT antes de responder.
        recetaDetalleRepository.flush();

        validarCantidadPersistida(
                detalles,
                detallesGuardados
        );

        final Usuario profesional =
                buscarProfesionalEmisor(
                        recetaGuardada
                );

        return RecetaResponse.desdeEntidades(
                recetaGuardada,
                detallesGuardados,
                profesional
        );
    }

    // Obtiene una receta concreta con sus medicamentos.
    @Transactional(readOnly = true)
    public RecetaResponse buscarPorId(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idRecetas
    ) {
        validarRutaReceta(
                idConsultorios,
                idPacientes,
                idConsultas,
                idRecetas
        );

        // Confirma la relación de la consulta.
        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        final Receta receta =
                buscarReceta(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idRecetas
                );

        return construirRespuesta(
                receta
        );
    }

    // Lista las recetas pertenecientes a una consulta.
    @Transactional(readOnly = true)
    public List<RecetaResponse> listarPorConsulta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final boolean incluirAnuladas
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

        final List<Receta> recetas =
                incluirAnuladas
                        ? recetaRepository
                        .findAllByConsultorioIdConsultoriosAndConsultaIdConsultasOrderByFechaEmisionDescIdRecetasDesc(
                                idConsultorios,
                                idConsultas
                        )
                        : recetaRepository
                        .findAllByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstadoOrderByFechaEmisionDescIdRecetasDesc(
                                idConsultorios,
                                idConsultas,
                                EstadoReceta.EMITIDA
                        );

        return convertirLista(
                recetas
        );
    }

    // Lista el historial de recetas del paciente.
    @Transactional(readOnly = true)
    public List<RecetaResponse> listarHistorialPaciente(
            final Long idConsultorios,
            final Long idPacientes,
            final boolean incluirAnuladas
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

        final List<Receta> recetas =
                incluirAnuladas
                        ? recetaRepository
                        .findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesOrderByFechaEmisionDescIdRecetasDesc(
                                idConsultorios,
                                idPacientes
                        )
                        : recetaRepository
                        .findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesAndEstadoOrderByFechaEmisionDescIdRecetasDesc(
                                idConsultorios,
                                idPacientes,
                                EstadoReceta.EMITIDA
                        );

        return convertirLista(
                recetas
        );
    }

    // Anula una receta sin borrar su contenido.
    @Transactional
    public RecetaResponse anular(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idRecetas,
            final Long idUsuarioResponsable,
            final String motivoAnulacion
    ) {
        validarIdentificadoresReceta(
                idConsultorios,
                idPacientes,
                idConsultas,
                idRecetas,
                idUsuarioResponsable
        );

        buscarConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        // Confirma que quien anula pertenece al consultorio.
        buscarProfesional(
                idUsuarioResponsable,
                idConsultorios
        );

        final Receta receta =
                buscarReceta(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idRecetas
                );

        if (receta.getEstado()
                == EstadoReceta.ANULADA) {
            throw new BusinessConflictException(
                    "PRESCRIPTION_ALREADY_CANCELLED",
                    "La receta ya se encuentra anulada."
            );
        }

        receta.anular(
                idUsuarioResponsable,
                motivoAnulacion
        );

        final Receta recetaAnulada =
                recetaRepository.saveAndFlush(
                        receta
                );

        return construirRespuesta(
                recetaAnulada
        );
    }

    // Cuenta las recetas emitidas del paciente.
    @Transactional(readOnly = true)
    public long contarEmitidasPaciente(
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

        return recetaRepository
                .countByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesAndEstado(
                        idConsultorios,
                        idPacientes,
                        EstadoReceta.EMITIDA
                );
    }

    // Construye medicamentos con orden consecutivo.
    private List<RecetaDetalle> construirDetalles(
            final Receta receta,
            final List<MedicamentoRequest> medicamentos
    ) {
        Objects.requireNonNull(
                receta,
                "La receta es obligatoria."
        );

        Objects.requireNonNull(
                receta.getConsultorio(),
                "El consultorio de la receta es obligatorio."
        );

        final List<RecetaDetalle> detalles =
                new ArrayList<>(
                        medicamentos.size()
                );

        for (int indice = 0;
             indice < medicamentos.size();
             indice++) {

            final MedicamentoRequest medicamento =
                    medicamentos.get(indice);

            if (medicamento == null) {
                throw new IllegalArgumentException(
                        "La receta contiene "
                                + "un medicamento inválido."
                );
            }

            final int posicion =
                    indice + 1;

            if (posicion > Short.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "La cantidad de medicamentos supera "
                                + "el límite permitido."
                );
            }

            final RecetaDetalle detalle =
                    new RecetaDetalle(
                            receta.getConsultorio(),
                            receta,
                            medicamento.medicamento(),
                            medicamento.presentacion(),
                            medicamento.dosis(),
                            medicamento.viaAdministracion(),
                            medicamento.frecuencia(),
                            medicamento.duracion(),
                            medicamento.indicaciones(),
                            (short) posicion
                    );

            detalles.add(
                    detalle
            );
        }

        return detalles;
    }

    // Valida la lista antes de guardar la cabecera.
    private static void validarMedicamentos(
            final List<MedicamentoRequest> medicamentos
    ) {
        if (medicamentos == null
                || medicamentos.isEmpty()) {
            throw new IllegalArgumentException(
                    "La receta debe contener "
                            + "al menos un medicamento."
            );
        }

        if (medicamentos.size()
                > MAXIMO_MEDICAMENTOS) {
            throw new IllegalArgumentException(
                    "La receta no puede contener más de "
                            + MAXIMO_MEDICAMENTOS
                            + " medicamentos."
            );
        }

        final Set<String> medicamentosNormalizados =
                new HashSet<>();

        for (final MedicamentoRequest medicamento
                : medicamentos) {

            if (medicamento == null) {
                throw new IllegalArgumentException(
                        "La receta contiene "
                                + "un medicamento inválido."
                );
            }

            if (medicamento.medicamento() == null
                    || medicamento.medicamento().isBlank()) {
                throw new IllegalArgumentException(
                        "El nombre del medicamento "
                                + "es obligatorio."
                );
            }

            final String claveMedicamento =
                    construirClaveMedicamento(
                            medicamento
                    );

            if (!medicamentosNormalizados.add(
                    claveMedicamento
            )) {
                throw new BusinessConflictException(
                        "DUPLICATE_PRESCRIPTION_ITEM",
                        "La receta contiene medicamentos "
                                + "duplicados con la misma "
                                + "presentación y dosis."
                );
            }
        }
    }

    // Detecta duplicados dentro de la solicitud.
    private static String construirClaveMedicamento(
            final MedicamentoRequest medicamento
    ) {
        return normalizarClave(
                medicamento.medicamento()
        )
                + "|"
                + normalizarClave(
                medicamento.presentacion()
        )
                + "|"
                + normalizarClave(
                medicamento.dosis()
        );
    }

    // Normaliza texto solo para comparar duplicados.
    private static String normalizarClave(
            final String valor
    ) {
        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    // Comprueba que se guardaran todos los detalles.
    private static void validarCantidadPersistida(
            final List<RecetaDetalle> detallesEsperados,
            final List<RecetaDetalle> detallesGuardados
    ) {
        if (detallesGuardados == null
                || detallesGuardados.size()
                != detallesEsperados.size()) {
            throw new IllegalStateException(
                    "No fue posible guardar todos "
                            + "los medicamentos de la receta."
            );
        }
    }

    // Construye una respuesta con medicamentos y profesional.
    private RecetaResponse construirRespuesta(
            final Receta receta
    ) {
        Objects.requireNonNull(
                receta,
                "La receta es obligatoria."
        );

        Objects.requireNonNull(
                receta.getConsultorio(),
                "El consultorio de la receta es obligatorio."
        );

        final Long idConsultorios =
                receta
                        .getConsultorio()
                        .getIdConsultorios();

        final Long idRecetas =
                receta.getIdRecetas();

        validarId(
                idConsultorios,
                "El identificador del consultorio "
                        + "de la receta no es válido."
        );

        validarId(
                idRecetas,
                "El identificador de la receta "
                        + "no es válido."
        );

        final List<RecetaDetalle> detalles =
                recetaDetalleRepository
                        .findAllByConsultorioIdConsultoriosAndRecetaIdRecetasOrderByOrdenAscIdRecetasDetalleAsc(
                                idConsultorios,
                                idRecetas
                        );

        final Usuario profesional =
                buscarProfesionalEmisor(
                        receta
                );

        return RecetaResponse.desdeEntidades(
                receta,
                detalles,
                profesional
        );
    }

    // Recupera al profesional emisor de la receta.
    private Usuario buscarProfesionalEmisor(
            final Receta receta
    ) {
        Objects.requireNonNull(
                receta,
                "La receta es obligatoria."
        );

        Objects.requireNonNull(
                receta.getConsultorio(),
                "El consultorio de la receta es obligatorio."
        );

        final Long idConsultorios =
                receta
                        .getConsultorio()
                        .getIdConsultorios();

        final Long idUsuarioEmisor =
                receta.getEmitidoPor();

        return buscarProfesional(
                idUsuarioEmisor,
                idConsultorios
        );
    }

    // Busca un profesional dentro de su consultorio.
    private Usuario buscarProfesional(
            final Long idUsuarios,
            final Long idConsultorios
    ) {
        validarId(
                idUsuarios,
                "El identificador del profesional "
                        + "no es válido."
        );

        validarId(
                idConsultorios,
                "El identificador del consultorio "
                        + "no es válido."
        );

        return usuarioRepository
                .findByIdUsuariosAndConsultorioIdConsultorios(
                        idUsuarios,
                        idConsultorios
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "PRESCRIPTION_PROFESSIONAL_NOT_FOUND",
                                "No se encontró al profesional "
                                        + "relacionado con la receta."
                        )
                );
    }

    // Convierte varias recetas en respuestas completas.
    private List<RecetaResponse> convertirLista(
            final List<Receta> recetas
    ) {
        Objects.requireNonNull(
                recetas,
                "La lista de recetas es obligatoria."
        );

        return recetas
                .stream()
                .filter(Objects::nonNull)
                .map(this::construirRespuesta)
                .toList();
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

    // Busca una receta dentro de toda su ruta clínica.
    private Receta buscarReceta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idRecetas
    ) {
        return recetaRepository
                .findByIdRecetasAndConsultorioIdConsultoriosAndConsultaIdConsultasAndConsultaPacienteIdPacientes(
                        idRecetas,
                        idConsultorios,
                        idConsultas,
                        idPacientes
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "PRESCRIPTION_NOT_FOUND",
                                "La receta médica solicitada "
                                        + "no existe."
                        )
                );
    }

    // Impide emitir desde una consulta anulada.
    private static void validarConsultaDisponible(
            final Consulta consulta
    ) {
        Objects.requireNonNull(
                consulta,
                "La consulta médica es obligatoria."
        );

        if (consulta.getEstado()
                == EstadoConsulta.ANULADA) {
            throw new BusinessConflictException(
                    "CANCELLED_CONSULTATION_NOT_AVAILABLE",
                    "No puede emitirse una receta "
                            + "desde una consulta anulada."
            );
        }

        if (consulta.getEstado()
                != EstadoConsulta.ABIERTA
                && consulta.getEstado()
                != EstadoConsulta.CERRADA) {
            throw new BusinessConflictException(
                    "CONSULTATION_NOT_AVAILABLE",
                    "La consulta no está disponible "
                            + "para emitir recetas."
            );
        }
    }

    // Valida la ruta de una consulta.
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

    // Valida la ruta completa de una receta.
    private static void validarRutaReceta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idRecetas
    ) {
        validarRutaConsulta(
                idConsultorios,
                idPacientes,
                idConsultas
        );

        validarId(
                idRecetas,
                "El identificador de la receta "
                        + "no es válido."
        );
    }

    // Valida las claves necesarias para emitir.
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

    // Valida las claves de una receta existente.
    private static void validarIdentificadoresReceta(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idRecetas,
            final Long idUsuarioResponsable
    ) {
        validarIdentificadores(
                idConsultorios,
                idPacientes,
                idConsultas,
                idUsuarioResponsable
        );

        validarId(
                idRecetas,
                "El identificador de la receta "
                        + "no es válido."
        );
    }

    // Comprueba que una clave sea positiva.
    private static void validarId(
            final Long identificador,
            final String mensaje
    ) {
        if (identificador == null
                || identificador <= 0) {
            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }
}