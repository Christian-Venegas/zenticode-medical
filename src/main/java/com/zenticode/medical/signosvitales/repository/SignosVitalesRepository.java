package com.zenticode.medical.signosvitales.repository;

import com.zenticode.medical.signosvitales.entity.SignosVitales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a signos vitales aislados por consultorio y consulta.
 */
@Repository
public interface SignosVitalesRepository
        extends JpaRepository<SignosVitales, Long> {

    // Busca un registro dentro del consultorio y consulta.
    Optional<SignosVitales>
    findByIdSignosVitalesAndConsultorioIdConsultoriosAndConsultaIdConsultas(
            Long idSignosVitales,
            Long idConsultorios,
            Long idConsultas
    );

    // Lista las mediciones de una consulta desde la más reciente.
    List<SignosVitales>
    findAllByConsultorioIdConsultoriosAndConsultaIdConsultasOrderByFechaRegistroDescIdSignosVitalesDesc(
            Long idConsultorios,
            Long idConsultas
    );

    // Lista la evolución completa de signos vitales del paciente.
    List<SignosVitales>
    findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesOrderByFechaRegistroDescIdSignosVitalesDesc(
            Long idConsultorios,
            Long idPacientes
    );

    // Comprueba si una consulta ya tiene mediciones registradas.
    boolean
    existsByConsultorioIdConsultoriosAndConsultaIdConsultas(
            Long idConsultorios,
            Long idConsultas
    );

    // Cuenta registros clínicos sin cruzar consultorios.
    long
    countByConsultorioIdConsultoriosAndConsultaPacienteIdPacientes(
            Long idConsultorios,
            Long idPacientes
    );
}