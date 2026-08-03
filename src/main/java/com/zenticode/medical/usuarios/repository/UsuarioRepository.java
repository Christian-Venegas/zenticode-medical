package com.zenticode.medical.usuarios.repository;

import com.zenticode.medical.usuarios.entity.Usuario;
import com.zenticode.medical.usuarios.entity.Usuario.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a usuarios aislados por consultorio.
 */
@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    // Busca un usuario por consultorio y correo.
    Optional<Usuario>
    findByConsultorioIdConsultoriosAndCorreo(
            Long idConsultorios,
            String correo
    );

    // Busca un usuario activo o con un estado concreto.
    Optional<Usuario>
    findByConsultorioIdConsultoriosAndCorreoAndEstado(
            Long idConsultorios,
            String correo,
            EstadoUsuario estado
    );

    // Comprueba si el correo existe dentro del consultorio.
    boolean
    existsByConsultorioIdConsultoriosAndCorreo(
            Long idConsultorios,
            String correo
    );

    // Comprueba el correo excluyendo un usuario concreto.
    boolean
    existsByConsultorioIdConsultoriosAndCorreoAndIdUsuariosNot(
            Long idConsultorios,
            String correo,
            Long idUsuarios
    );

    // Busca un usuario dentro de su consultorio propietario.
    Optional<Usuario>
    findByIdUsuariosAndConsultorioIdConsultorios(
            Long idUsuarios,
            Long idConsultorios
    );

    // Busca un usuario por consultorio y estado.
    Optional<Usuario>
    findByIdUsuariosAndConsultorioIdConsultoriosAndEstado(
            Long idUsuarios,
            Long idConsultorios,
            EstadoUsuario estado
    );

    // Lista los usuarios ordenados por apellidos y nombres.
    List<Usuario>
    findAllByConsultorioIdConsultoriosOrderByApellidosAscNombresAsc(
            Long idConsultorios
    );

    // Lista usuarios del consultorio filtrados por estado.
    List<Usuario>
    findAllByConsultorioIdConsultoriosAndEstadoOrderByApellidosAscNombresAsc(
            Long idConsultorios,
            EstadoUsuario estado
    );

    // Comprueba si el usuario pertenece al consultorio y está activo.
    boolean
    existsByIdUsuariosAndConsultorioIdConsultoriosAndEstado(
            Long idUsuarios,
            Long idConsultorios,
            EstadoUsuario estado
    );
}
