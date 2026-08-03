package com.zenticode.medical.roles.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa un rol de autorización del sistema.
 */
@Entity
@Table(name = "roles")
public class Rol {

    // Clave primaria generada por PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_roles", nullable = false)
    private Long idRoles;

    // Código estable utilizado por Spring Security.
    @Column(
            name = "codigo",
            nullable = false,
            unique = true,
            length = 50
    )
    private String codigo;

    // Nombre legible mostrado en la interfaz.
    @Column(
            name = "nombre",
            nullable = false,
            length = 100
    )
    private String nombre;

    // Explicación opcional de las capacidades del rol.
    @Column(
            name = "descripcion",
            length = 255
    )
    private String descripcion;

    // Indica si el rol puede seguir asignándose.
    @Column(
            name = "activo",
            nullable = false
    )
    private boolean activo;

    // Fecha administrada automáticamente por PostgreSQL.
    @Column(
            name = "fecha_creacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaCreacion;

    // Constructor requerido por JPA.
    protected Rol() {
    }

    // Crea un rol activo con código único.
    public Rol(
            final String codigo,
            final String nombre,
            final String descripcion
    ) {
        this.codigo = normalizarCodigo(codigo);

        this.nombre = validarTextoObligatorio(
                nombre,
                "El nombre del rol es obligatorio."
        );

        this.descripcion =
                normalizarTextoOpcional(descripcion);

        this.activo = true;
    }

    // Actualiza la información visible del rol.
    public void actualizar(
            final String nombre,
            final String descripcion
    ) {
        this.nombre = validarTextoObligatorio(
                nombre,
                "El nombre del rol es obligatorio."
        );

        this.descripcion =
                normalizarTextoOpcional(descripcion);
    }

    // Habilita nuevamente el rol.
    public void activar() {
        this.activo = true;
    }

    // Deshabilita el rol sin eliminarlo.
    public void desactivar() {
        this.activo = false;
    }

    // Normaliza códigos como ADMIN_CONSULTORIO.
    private static String normalizarCodigo(
            final String valor
    ) {
        final String codigoNormalizado =
                validarTextoObligatorio(
                        valor,
                        "El código del rol es obligatorio."
                )
                        .toUpperCase(Locale.ROOT)
                        .replace('-', '_')
                        .replace(' ', '_');

        if (!codigoNormalizado.matches("^[A-Z][A-Z0-9_]*$")) {
            throw new IllegalArgumentException(
                    "El código del rol no tiene un formato válido."
            );
        }

        if (codigoNormalizado.length() > 50) {
            throw new IllegalArgumentException(
                    "El código del rol no puede superar los 50 caracteres."
            );
        }

        return codigoNormalizado;
    }

    // Valida y limpia un texto obligatorio.
    private static String validarTextoObligatorio(
            final String valor,
            final String mensajeError
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeError);
        }

        return valor.trim();
    }

    // Convierte textos opcionales vacíos en null.
    private static String normalizarTextoOpcional(
            final String valor
    ) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    public Long getIdRoles() {
        return idRoles;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    // Compara roles mediante su clave primaria persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Rol rol)) {
            return false;
        }

        return idRoles != null
                && Objects.equals(
                idRoles,
                rol.idRoles
        );
    }

    // Mantiene un hash estable para entidades JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}