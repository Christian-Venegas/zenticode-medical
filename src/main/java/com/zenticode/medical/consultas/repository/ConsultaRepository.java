package com.zenticode.medical.consultas.repository;

import com.zenticode.medical.consultas.entity.Consulta;
import com.zenticode.medical.consultas.entity.Consulta.EstadoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a consultas médicas aisladas por consultorio.
 */
@Repository
public interface ConsultaRepository
        extends JpaRepository<Consulta, Long> {

    // Busca una consulta dentro del paciente y consultorio.
    Optional<Consulta>
    findByIdConsultasAndConsultorioIdConsultoriosAndPacienteIdPacientes(
            Long idConsultas,
            Long idConsultorios,
            Long idPacientes
    );

    // Busca una consulta vinculada a su historia clínica.
    Optional<Consulta>
    findByIdConsultasAndConsultorioIdConsultoriosAndPacienteIdPacientesAndHistoriaClinicaIdHistoriasClinicas(
            Long idConsultas,
            Long idConsultorios,
            Long idPacientes,
            Long idHistoriasClinicas
    );

    // Lista el historial completo, mostrando primero lo más reciente.
    List<Consulta>
    findAllByConsultorioIdConsultoriosAndPacienteIdPacientesOrderByFechaHoraAtencionDescIdConsultasDesc(
            Long idConsultorios,
            Long idPacientes
    );

    // Lista las consultas de una historia clínica concreta.
    List<Consulta>
    findAllByConsultorioIdConsultoriosAndPacienteIdPacientesAndHistoriaClinicaIdHistoriasClinicasOrderByFechaHoraAtencionDescIdConsultasDesc(
            Long idConsultorios,
            Long idPacientes,
            Long idHistoriasClinicas
    );

    // Lista consultas por estado dentro del historial.
    List<Consulta>
    findAllByConsultorioIdConsultoriosAndPacienteIdPacientesAndEstadoOrderByFechaHoraAtencionDescIdConsultasDesc(
            Long idConsultorios,
            Long idPacientes,
            EstadoConsulta estado
    );

    // Comprueba si existe una consulta asociada a una cita.
    boolean
    existsByConsultorioIdConsultoriosAndIdCitas(
            Long idConsultorios,
            Long idCitas
    );
}