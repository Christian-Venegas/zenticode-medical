package com.zenticode.medical.recetas.repository;

import com.zenticode.medical.recetas.entity.Receta;
import com.zenticode.medical.recetas.entity.Receta.EstadoReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a recetas médicas aisladas por consultorio.
 */
@Repository
public interface RecetaRepository
        extends JpaRepository<Receta, Long> {

    // Busca una receta dentro del consultorio y consulta.
    Optional<Receta>
    findByIdRecetasAndConsultorioIdConsultoriosAndConsultaIdConsultas(
            Long idRecetas,
            Long idConsultorios,
            Long idConsultas
    );

    // Busca una receta validando también el paciente.
    Optional<Receta>
    findByIdRecetasAndConsultorioIdConsultoriosAndConsultaIdConsultasAndConsultaPacienteIdPacientes(
            Long idRecetas,
            Long idConsultorios,
            Long idConsultas,
            Long idPacientes
    );

    // Lista todas las recetas de una consulta.
    List<Receta>
    findAllByConsultorioIdConsultoriosAndConsultaIdConsultasOrderByFechaEmisionDescIdRecetasDesc(
            Long idConsultorios,
            Long idConsultas
    );

    // Lista las recetas de una consulta según su estado.
    List<Receta>
    findAllByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstadoOrderByFechaEmisionDescIdRecetasDesc(
            Long idConsultorios,
            Long idConsultas,
            EstadoReceta estado
    );

    // Lista el historial completo de recetas del paciente.
    List<Receta>
    findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesOrderByFechaEmisionDescIdRecetasDesc(
            Long idConsultorios,
            Long idPacientes
    );

    // Lista las recetas del paciente según su estado.
    List<Receta>
    findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesAndEstadoOrderByFechaEmisionDescIdRecetasDesc(
            Long idConsultorios,
            Long idPacientes,
            EstadoReceta estado
    );

    // Comprueba si la consulta posee una receta emitida.
    boolean
    existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstado(
            Long idConsultorios,
            Long idConsultas,
            EstadoReceta estado
    );

    // Cuenta las recetas del paciente por estado.
    long
    countByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesAndEstado(
            Long idConsultorios,
            Long idPacientes,
            EstadoReceta estado
    );
}