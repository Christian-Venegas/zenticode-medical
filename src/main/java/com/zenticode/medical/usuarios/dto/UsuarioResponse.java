package com.zenticode.medical.usuarios.dto;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.entity.Usuario.EstadoUsuario;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Información segura de un usuario para respuestas de la API.
 */
public record UsuarioResponse(

        Long idUsuarios,

        Long idConsultorios,

        String correo,

        String nombres,

        String apellidos,

        String numeroColegiatura,

        String telefono,

        EstadoUsuario estado,

        OffsetDateTime ultimoAcceso,

        OffsetDateTime fechaCreacion,

        OffsetDateTime fechaModificacion

) {

    // Convierte una entidad Usuario en una respuesta segura.
    public static UsuarioResponse desde(
            final Usuario usuario
    ) {
        Objects.requireNonNull(
                usuario,
                "El usuario es obligatorio para construir la respuesta."
        );

        // La relación con consultorio siempre es obligatoria.
        final Consultorio consultorio =
                Objects.requireNonNull(
                        usuario.getConsultorio(),
                        "El consultorio del usuario es obligatorio."
                );

        return new UsuarioResponse(
                usuario.getIdUsuarios(),
                consultorio.getIdConsultorios(),
                usuario.getCorreo(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getNumeroColegiatura(),
                usuario.getTelefono(),
                usuario.getEstado(),
                usuario.getUltimoAcceso(),
                usuario.getFechaCreacion(),
                usuario.getFechaModificacion()
        );
    }
}