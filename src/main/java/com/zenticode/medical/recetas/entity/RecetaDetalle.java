package com.zenticode.medical.recetas.entity;

import com.zenticode.medical.consultorios.entity.Consultorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Representa un medicamento incluido en una receta médica.
 */
@Entity
@Table(name = "recetas_detalle")
public class RecetaDetalle {

    // Clave primaria del detalle de receta.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_recetas_detalle",
            nullable = false
    )
    private Long idRecetasDetalle;

    // Consultorio propietario del detalle.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false,
            updatable = false
    )
    private Consultorio consultorio;

    // Receta a la que pertenece el medicamento.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_recetas",
            nullable = false,
            updatable = false
    )
    private Receta receta;

    // Nombre del medicamento prescrito.
    @Column(
            name = "medicamento",
            nullable = false,
            length = 200
    )
    private String medicamento;

    // Forma o presentación farmacéutica.
    @Column(
            name = "presentacion",
            length = 120
    )
    private String presentacion;

    // Cantidad que debe administrarse por toma.
    @Column(
            name = "dosis",
            nullable = false,
            length = 120
    )
    private String dosis;

    // Vía por la cual se administra el medicamento.
    @Column(
            name = "via_administracion",
            length = 80
    )
    private String viaAdministracion;

    // Intervalo o frecuencia de administración.
    @Column(
            name = "frecuencia",
            nullable = false,
            length = 120
    )
    private String frecuencia;

    // Tiempo total indicado para el tratamiento.
    @Column(
            name = "duracion",
            nullable = false,
            length = 120
    )
    private String duracion;

    // Indicaciones específicas del medicamento.
    @Column(
            name = "indicaciones",
            length = 500
    )
    private String indicaciones;

    // Posición visual del medicamento en la receta.
    @Column(
            name = "orden",
            nullable = false
    )
    private Short orden;

    // Constructor requerido por JPA.
    protected RecetaDetalle() {
    }

    // Registra un medicamento dentro de una receta.
    public RecetaDetalle(
            final Consultorio consultorio,
            final Receta receta,
            final String medicamento,
            final String presentacion,
            final String dosis,
            final String viaAdministracion,
            final String frecuencia,
            final String duracion,
            final String indicaciones,
            final Short orden
    ) {
        this.consultorio =
                Objects.requireNonNull(
                        consultorio,
                        "El consultorio es obligatorio."
                );

        this.receta =
                Objects.requireNonNull(
                        receta,
                        "La receta médica es obligatoria."
                );

        validarMismoConsultorio(
                consultorio,
                receta
        );

        this.medicamento =
                validarTextoObligatorio(
                        medicamento,
                        200,
                        "El medicamento es obligatorio.",
                        "El medicamento no puede superar "
                                + "los 200 caracteres."
                );

        this.presentacion =
                normalizarTextoOpcional(
                        presentacion,
                        120,
                        "La presentación no puede superar "
                                + "los 120 caracteres."
                );

        this.dosis =
                validarTextoObligatorio(
                        dosis,
                        120,
                        "La dosis es obligatoria.",
                        "La dosis no puede superar "
                                + "los 120 caracteres."
                );

        this.viaAdministracion =
                normalizarTextoOpcional(
                        viaAdministracion,
                        80,
                        "La vía de administración no puede superar "
                                + "los 80 caracteres."
                );

        this.frecuencia =
                validarTextoObligatorio(
                        frecuencia,
                        120,
                        "La frecuencia es obligatoria.",
                        "La frecuencia no puede superar "
                                + "los 120 caracteres."
                );

        this.duracion =
                validarTextoObligatorio(
                        duracion,
                        120,
                        "La duración es obligatoria.",
                        "La duración no puede superar "
                                + "los 120 caracteres."
                );

        this.indicaciones =
                normalizarTextoOpcional(
                        indicaciones,
                        500,
                        "Las indicaciones no pueden superar "
                                + "los 500 caracteres."
                );

        this.orden =
                validarOrden(orden);
    }

    // Comprueba que la receta pertenezca al consultorio.
    private static void validarMismoConsultorio(
            final Consultorio consultorio,
            final Receta receta
    ) {
        if (receta.getConsultorio() == null) {
            throw new IllegalArgumentException(
                    "La receta no tiene un consultorio válido."
            );
        }

        final Long idConsultorios =
                consultorio.getIdConsultorios();

        final Long idConsultoriosReceta =
                receta
                        .getConsultorio()
                        .getIdConsultorios();

        if (idConsultorios != null
                && idConsultoriosReceta != null) {

            if (!Objects.equals(
                    idConsultorios,
                    idConsultoriosReceta
            )) {
                throw new IllegalArgumentException(
                        "La receta no pertenece al consultorio."
                );
            }

            return;
        }

        // Protege también entidades nuevas aún no persistidas.
        if (receta.getConsultorio() != consultorio) {
            throw new IllegalArgumentException(
                    "La receta no pertenece al consultorio."
            );
        }
    }

    // Valida un campo obligatorio.
    private static String validarTextoObligatorio(
            final String valor,
            final int longitudMaxima,
            final String mensajeObligatorio,
            final String mensajeLongitud
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    mensajeObligatorio
            );
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    mensajeLongitud
            );
        }

        return texto;
    }

    // Normaliza un campo opcional.
    private static String normalizarTextoOpcional(
            final String valor,
            final int longitudMaxima,
            final String mensajeLongitud
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        final String texto =
                valor.trim();

        if (texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    mensajeLongitud
            );
        }

        return texto;
    }

    // Valida la posición del medicamento.
    private static Short validarOrden(
            final Short valor
    ) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException(
                    "El orden del medicamento debe ser "
                            + "mayor que cero."
            );
        }

        return valor;
    }

    public Long getIdRecetasDetalle() {
        return idRecetasDetalle;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Receta getReceta() {
        return receta;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public String getDosis() {
        return dosis;
    }

    public String getViaAdministracion() {
        return viaAdministracion;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public Short getOrden() {
        return orden;
    }

    // Compara detalles mediante su PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof RecetaDetalle detalle)) {
            return false;
        }

        return idRecetasDetalle != null
                && Objects.equals(
                idRecetasDetalle,
                detalle.idRecetasDetalle
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}