package com.zenticode.medical.recetas.repository;

import com.zenticode.medical.recetas.entity.RecetaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a medicamentos de recetas aislados por consultorio.
 */
@Repository
public interface RecetaDetalleRepository
        extends JpaRepository<RecetaDetalle, Long> {

    // Busca un medicamento dentro de su receta y consultorio.
    Optional<RecetaDetalle>
    findByIdRecetasDetalleAndConsultorioIdConsultoriosAndRecetaIdRecetas(
            Long idRecetasDetalle,
            Long idConsultorios,
            Long idRecetas
    );

    // Lista los medicamentos de una receta en su orden de impresión.
    List<RecetaDetalle>
    findAllByConsultorioIdConsultoriosAndRecetaIdRecetasOrderByOrdenAscIdRecetasDetalleAsc(
            Long idConsultorios,
            Long idRecetas
    );

    // Comprueba si una posición ya está ocupada en la receta.
    boolean
    existsByConsultorioIdConsultoriosAndRecetaIdRecetasAndOrden(
            Long idConsultorios,
            Long idRecetas,
            Short orden
    );

    // Comprueba una posición excluyendo el detalle consultado.
    boolean
    existsByConsultorioIdConsultoriosAndRecetaIdRecetasAndOrdenAndIdRecetasDetalleNot(
            Long idConsultorios,
            Long idRecetas,
            Short orden,
            Long idRecetasDetalle
    );

    // Comprueba si la receta contiene medicamentos.
    boolean
    existsByConsultorioIdConsultoriosAndRecetaIdRecetas(
            Long idConsultorios,
            Long idRecetas
    );

    // Cuenta los medicamentos pertenecientes a una receta.
    long
    countByConsultorioIdConsultoriosAndRecetaIdRecetas(
            Long idConsultorios,
            Long idRecetas
    );
}