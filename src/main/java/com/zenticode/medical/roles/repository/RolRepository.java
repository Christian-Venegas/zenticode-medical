package com.zenticode.medical.roles.repository;

import com.zenticode.medical.roles.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de roles.
 */
@Repository
public interface RolRepository
        extends JpaRepository<Rol, Long> {

    // Busca un rol por su código estable.
    Optional<Rol> findByCodigo(
            String codigo
    );

    // Busca únicamente un rol activo.
    Optional<Rol> findByCodigoAndActivoTrue(
            String codigo
    );

    // Comprueba si un código ya está registrado.
    boolean existsByCodigo(
            String codigo
    );

    // Obtiene roles activos a partir de varios códigos.
    List<Rol> findAllByCodigoInAndActivoTrue(
            Collection<String> codigos
    );

    // Lista los roles activos ordenados por nombre.
    List<Rol> findAllByActivoTrueOrderByNombreAsc();
}