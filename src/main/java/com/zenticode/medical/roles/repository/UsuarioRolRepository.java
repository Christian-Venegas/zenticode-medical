package com.zenticode.medical.roles.repository;

import com.zenticode.medical.roles.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a las asignaciones de roles.
 */
@Repository
public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, Long> {

    // Comprueba si el usuario ya tiene el rol dentro del consultorio.
    boolean existsByConsultorioIdConsultoriosAndUsuarioIdUsuariosAndRolIdRoles(
            Long idConsultorios,
            Long idUsuarios,
            Long idRoles
    );

    // Busca una asignación concreta dentro del consultorio.
    Optional<UsuarioRol>
    findByConsultorioIdConsultoriosAndUsuarioIdUsuariosAndRolIdRoles(
            Long idConsultorios,
            Long idUsuarios,
            Long idRoles
    );

    // Lista las asignaciones de un usuario dentro de su consultorio.
    List<UsuarioRol>
    findAllByConsultorioIdConsultoriosAndUsuarioIdUsuarios(
            Long idConsultorios,
            Long idUsuarios
    );

    // Obtiene únicamente los códigos de roles activos del usuario.
    @Query("""
            SELECT ur.rol.codigo
            FROM UsuarioRol ur
            WHERE ur.consultorio.idConsultorios = :idConsultorios
              AND ur.usuario.idUsuarios = :idUsuarios
              AND ur.rol.activo = true
            ORDER BY ur.rol.codigo
            """)
    List<String> buscarCodigosRolesActivos(
            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idUsuarios")
            Long idUsuarios
    );

    // Comprueba si el usuario posee un código de rol activo.
    @Query("""
            SELECT CASE
                       WHEN COUNT(ur) > 0 THEN true
                       ELSE false
                   END
            FROM UsuarioRol ur
            WHERE ur.consultorio.idConsultorios = :idConsultorios
              AND ur.usuario.idUsuarios = :idUsuarios
              AND ur.rol.codigo = :codigoRol
              AND ur.rol.activo = true
            """)
    boolean tieneRolActivo(
            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idUsuarios")
            Long idUsuarios,

            @Param("codigoRol")
            String codigoRol
    );

    // Elimina una asignación concreta respetando el consultorio.
    @Modifying
    void deleteByConsultorioIdConsultoriosAndUsuarioIdUsuariosAndRolIdRoles(
            Long idConsultorios,
            Long idUsuarios,
            Long idRoles
    );
}