package com.zenticode.medical.roles.entity;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.usuarios.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Representa la asignación de un rol a un usuario.
 */
@Entity
@Table(
        name = "usuarios_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_usuarios_roles",
                        columnNames = {
                                "id_consultorios",
                                "id_usuarios",
                                "id_roles"
                        }
                )
        }
)
public class UsuarioRol {

    // Clave primaria generada por PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id_usuarios_roles",
            nullable = false
    )
    private Long idUsuariosRoles;

    // Consultorio propietario de la asignación.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_consultorios",
            nullable = false
    )
    private Consultorio consultorio;

    // Usuario que recibe el rol.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_usuarios",
            nullable = false
    )
    private Usuario usuario;

    // Rol asignado al usuario.
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_roles",
            nullable = false
    )
    private Rol rol;

    // Fecha generada automáticamente por PostgreSQL.
    @Column(
            name = "fecha_asignacion",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime fechaAsignacion;

    // Usuario que realizó la asignación.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por")
    private Usuario asignadoPor;

    // Constructor requerido por JPA.
    protected UsuarioRol() {
    }

    // Crea una asignación inicial sin asignador.
    public UsuarioRol(
            final Consultorio consultorio,
            final Usuario usuario,
            final Rol rol
    ) {
        this(
                consultorio,
                usuario,
                rol,
                null
        );
    }

    // Crea una asignación indicando quién la realizó.
    public UsuarioRol(
            final Consultorio consultorio,
            final Usuario usuario,
            final Rol rol,
            final Usuario asignadoPor
    ) {
        this.consultorio = Objects.requireNonNull(
                consultorio,
                "El consultorio de la asignación es obligatorio."
        );

        this.usuario = Objects.requireNonNull(
                usuario,
                "El usuario de la asignación es obligatorio."
        );

        this.rol = Objects.requireNonNull(
                rol,
                "El rol de la asignación es obligatorio."
        );

        this.asignadoPor = asignadoPor;

        validarMismoConsultorio();
    }

    // Comprueba que usuario y asignador pertenezcan al consultorio.
    private void validarMismoConsultorio() {
        if (!perteneceAlConsultorio(usuario)) {
            throw new IllegalArgumentException(
                    "El usuario no pertenece al consultorio indicado."
            );
        }

        if (asignadoPor != null
                && !perteneceAlConsultorio(asignadoPor)) {
            throw new IllegalArgumentException(
                    "El usuario asignador no pertenece al consultorio."
            );
        }
    }

    // Compara el consultorio mediante su clave persistida.
    private boolean perteneceAlConsultorio(
            final Usuario usuarioEvaluado
    ) {
        final Consultorio consultorioUsuario =
                usuarioEvaluado.getConsultorio();

        if (consultorioUsuario == null) {
            return false;
        }

        final Long idConsultoriosEsperado =
                consultorio.getIdConsultorios();

        final Long idConsultoriosUsuario =
                consultorioUsuario.getIdConsultorios();

        // Durante pruebas con entidades nuevas se compara la instancia.
        if (idConsultoriosEsperado == null
                || idConsultoriosUsuario == null) {
            return consultorioUsuario == consultorio;
        }

        // En entidades persistidas se compara la PK.
        return Objects.equals(
                idConsultoriosEsperado,
                idConsultoriosUsuario
        );
    }

    public Long getIdUsuariosRoles() {
        return idUsuariosRoles;
    }

    public Consultorio getConsultorio() {
        return consultorio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public OffsetDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public Usuario getAsignadoPor() {
        return asignadoPor;
    }

    // Compara asignaciones mediante la PK persistida.
    @Override
    public boolean equals(final Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof UsuarioRol usuarioRol)) {
            return false;
        }

        return idUsuariosRoles != null
                && Objects.equals(
                idUsuariosRoles,
                usuarioRol.idUsuariosRoles
        );
    }

    // Mantiene un hash estable para JPA.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}