package com.zenticode.medical.recetas.dto;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.recetas.entity.Receta;
import com.zenticode.medical.recetas.entity.Receta.EstadoReceta;
import com.zenticode.medical.recetas.entity.RecetaDetalle;
import com.zenticode.medical.usuarios.entity.Usuario;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Información completa y controlada de una receta médica.
 */
public record RecetaResponse(

        Long idRecetas,

        Long idConsultorios,

        Long idPacientes,

        Long idConsultas,

        String nombrePaciente,

        String nombreConsultorio,

        String descripcionConsultorio,

        String direccionConsultorio,

        String telefonoConsultorio,

        String correoConsultorio,

        String logoUrlConsultorio,

        String nombreProfesional,

        String especialidadProfesional,

        String numeroColegiatura,

        String firmaUrlProfesional,

        String selloUrlProfesional,

        OffsetDateTime fechaEmision,

        String indicacionesGenerales,

        EstadoReceta estado,

        boolean anulable,

        String motivoAnulacion,

        OffsetDateTime fechaAnulacion,

        Long anuladoPor,

        Long emitidoPor,

        OffsetDateTime fechaCreacion,

        int totalMedicamentos,

        List<MedicamentoResponse> medicamentos

) {

    // Convierte la receta sin un perfil profesional cargado.
    public static RecetaResponse desdeEntidades(
            final Receta receta,
            final List<RecetaDetalle> detalles
    ) {
        return desdeEntidades(
                receta,
                detalles,
                null
        );
    }

    // Convierte la receta con sus datos profesionales.
    public static RecetaResponse desdeEntidades(
            final Receta receta,
            final List<RecetaDetalle> detalles,
            final Usuario profesional
    ) {
        Objects.requireNonNull(
                receta,
                "La receta médica es obligatoria."
        );

        final Consultorio consultorio =
                Objects.requireNonNull(
                        receta.getConsultorio(),
                        "El consultorio de la receta es obligatorio."
                );

        Objects.requireNonNull(
                receta.getConsulta(),
                "La consulta de la receta es obligatoria."
        );

        Objects.requireNonNull(
                receta
                        .getConsulta()
                        .getPaciente(),
                "El paciente de la receta es obligatorio."
        );

        Objects.requireNonNull(
                detalles,
                "Los medicamentos de la receta son obligatorios."
        );

        validarProfesional(
                receta,
                consultorio,
                profesional
        );

        final List<MedicamentoResponse> medicamentosOrdenados =
                detalles
                        .stream()
                        .filter(Objects::nonNull)
                        .sorted(
                                Comparator
                                        .comparing(
                                                RecetaDetalle::getOrden,
                                                Comparator.nullsLast(
                                                        Comparator.naturalOrder()
                                                )
                                        )
                                        .thenComparing(
                                                RecetaDetalle
                                                        ::getIdRecetasDetalle,
                                                Comparator.nullsLast(
                                                        Comparator.naturalOrder()
                                                )
                                        )
                        )
                        .map(
                                MedicamentoResponse::desdeEntidad
                        )
                        .toList();

        final boolean recetaAnulable =
                receta.getEstado()
                        == EstadoReceta.EMITIDA;

        return new RecetaResponse(
                receta.getIdRecetas(),
                consultorio.getIdConsultorios(),
                receta
                        .getConsulta()
                        .getPaciente()
                        .getIdPacientes(),
                receta
                        .getConsulta()
                        .getIdConsultas(),
                construirNombrePaciente(receta),
                valorObligatorioOAlternativo(
                        consultorio.getNombre(),
                        "Consultorio médico"
                ),
                valorOpcional(
                        consultorio.getDescripcionDocumentos()
                ),
                valorOpcional(
                        consultorio.getDireccion()
                ),
                valorOpcional(
                        consultorio.getTelefono()
                ),
                valorOpcional(
                        consultorio.getCorreo()
                ),
                valorOpcional(
                        consultorio.getLogoUrl()
                ),
                construirNombreProfesional(
                        profesional
                ),
                obtenerEspecialidad(
                        profesional
                ),
                obtenerNumeroColegiatura(
                        profesional
                ),
                obtenerFirmaUrl(
                        profesional
                ),
                obtenerSelloUrl(
                        profesional
                ),
                receta.getFechaEmision(),
                receta.getIndicacionesGenerales(),
                receta.getEstado(),
                recetaAnulable,
                receta.getMotivoAnulacion(),
                receta.getFechaAnulacion(),
                receta.getAnuladoPor(),
                receta.getEmitidoPor(),
                receta.getFechaCreacion(),
                medicamentosOrdenados.size(),
                List.copyOf(
                        medicamentosOrdenados
                )
        );
    }

    // Confirma que el profesional pertenezca al consultorio.
    private static void validarProfesional(
            final Receta receta,
            final Consultorio consultorio,
            final Usuario profesional
    ) {
        if (profesional == null) {
            return;
        }

        if (profesional.getIdUsuarios() == null
                || !Objects.equals(
                profesional.getIdUsuarios(),
                receta.getEmitidoPor()
        )) {
            throw new IllegalArgumentException(
                    "El profesional no corresponde "
                            + "al emisor de la receta."
            );
        }

        if (profesional.getConsultorio() == null
                || !Objects.equals(
                profesional
                        .getConsultorio()
                        .getIdConsultorios(),
                consultorio.getIdConsultorios()
        )) {
            throw new IllegalArgumentException(
                    "El profesional no pertenece "
                            + "al consultorio de la receta."
            );
        }
    }

    // Construye el nombre completo del paciente.
    private static String construirNombrePaciente(
            final Receta receta
    ) {
        final String nombres =
                receta
                        .getConsulta()
                        .getPaciente()
                        .getNombres();

        final String apellidos =
                receta
                        .getConsulta()
                        .getPaciente()
                        .getApellidos();

        return unirNombreCompleto(
                nombres,
                apellidos,
                "Paciente sin nombre"
        );
    }

    // Construye el nombre completo del profesional.
    private static String construirNombreProfesional(
            final Usuario profesional
    ) {
        if (profesional == null) {
            return null;
        }

        return unirNombreCompleto(
                profesional.getNombres(),
                profesional.getApellidos(),
                "Profesional no identificado"
        );
    }

    // Obtiene la especialidad profesional.
    private static String obtenerEspecialidad(
            final Usuario profesional
    ) {
        if (profesional == null) {
            return null;
        }

        return valorOpcional(
                profesional.getEspecialidad()
        );
    }

    // Obtiene el número de colegiatura.
    private static String obtenerNumeroColegiatura(
            final Usuario profesional
    ) {
        if (profesional == null) {
            return null;
        }

        return valorOpcional(
                profesional.getNumeroColegiatura()
        );
    }

    // Obtiene la referencia segura de la firma.
    private static String obtenerFirmaUrl(
            final Usuario profesional
    ) {
        if (profesional == null) {
            return null;
        }

        return valorOpcional(
                profesional.getFirmaUrl()
        );
    }

    // Obtiene la referencia segura del sello.
    private static String obtenerSelloUrl(
            final Usuario profesional
    ) {
        if (profesional == null) {
            return null;
        }

        return valorOpcional(
                profesional.getSelloUrl()
        );
    }

    // Une nombres y apellidos sin espacios sobrantes.
    private static String unirNombreCompleto(
            final String nombres,
            final String apellidos,
            final String valorAlternativo
    ) {
        final String nombresSeguros =
                nombres == null
                        ? ""
                        : nombres.trim();

        final String apellidosSeguros =
                apellidos == null
                        ? ""
                        : apellidos.trim();

        final String nombreCompleto =
                (
                        nombresSeguros
                                + " "
                                + apellidosSeguros
                ).trim();

        return nombreCompleto.isBlank()
                ? valorAlternativo
                : nombreCompleto;
    }

    // Normaliza valores opcionales de respuesta.
    private static String valorOpcional(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    // Utiliza una alternativa para datos obligatorios ausentes.
    private static String valorObligatorioOAlternativo(
            final String valor,
            final String alternativa
    ) {
        final String valorSeguro =
                valorOpcional(valor);

        return valorSeguro == null
                ? alternativa
                : valorSeguro;
    }

    /**
     * Medicamento incluido en una receta médica.
     */
    public record MedicamentoResponse(

            Long idRecetasDetalle,

            String medicamento,

            String presentacion,

            String dosis,

            String viaAdministracion,

            String frecuencia,

            String duracion,

            String indicaciones,

            Short orden

    ) {

        // Convierte el detalle en una respuesta controlada.
        public static MedicamentoResponse desdeEntidad(
                final RecetaDetalle detalle
        ) {
            Objects.requireNonNull(
                    detalle,
                    "El detalle de receta es obligatorio."
            );

            Objects.requireNonNull(
                    detalle.getIdRecetasDetalle(),
                    "El identificador del detalle "
                            + "de receta es obligatorio."
            );

            Objects.requireNonNull(
                    detalle.getOrden(),
                    "El orden del medicamento es obligatorio."
            );

            return new MedicamentoResponse(
                    detalle.getIdRecetasDetalle(),
                    detalle.getMedicamento(),
                    detalle.getPresentacion(),
                    detalle.getDosis(),
                    detalle.getViaAdministracion(),
                    detalle.getFrecuencia(),
                    detalle.getDuracion(),
                    detalle.getIndicaciones(),
                    detalle.getOrden()
            );
        }
    }
}