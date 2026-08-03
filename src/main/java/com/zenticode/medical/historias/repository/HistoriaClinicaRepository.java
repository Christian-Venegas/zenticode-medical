package com.zenticode.medical.historias.repository;

import com.zenticode.medical.historias.entity.HistoriaClinica;
import com.zenticode.medical.historias.entity.HistoriaClinica.EstadoHistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a historias clínicas aisladas por consultorio.
 */
@Repository
public interface HistoriaClinicaRepository
        extends JpaRepository<HistoriaClinica, Long> {

    // Busca la historia clínica activa de un paciente.
    Optional<HistoriaClinica>
    findByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstado(
            Long idConsultorios,
            Long idPacientes,
            EstadoHistoriaClinica estado
    );

    // Busca una historia específica dentro del consultorio.
    Optional<HistoriaClinica>
    findByIdHistoriasClinicasAndConsultorioIdConsultoriosAndPacienteIdPacientes(
            Long idHistoriasClinicas,
            Long idConsultorios,
            Long idPacientes
    );

    // Comprueba si el paciente ya tiene una historia activa.
    boolean
    existsByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstado(
            Long idConsultorios,
            Long idPacientes,
            EstadoHistoriaClinica estado
    );
}