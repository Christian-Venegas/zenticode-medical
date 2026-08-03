package com.zenticode.medical.diagnosticos.repository;

import com.zenticode.medical.diagnosticos.entity.DiagnosticoConsulta;
import com.zenticode.medical.diagnosticos.entity.DiagnosticoConsulta.EstadoDiagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a diagnósticos aislados por consultorio y consulta.
 */
@Repository
public interface DiagnosticoConsultaRepository
        extends JpaRepository<DiagnosticoConsulta, Long> {

    // Busca un diagnóstico dentro del consultorio y consulta.
    Optional<DiagnosticoConsulta>
    findByIdDiagnosticosConsultasAndConsultorioIdConsultoriosAndConsultaIdConsultas(
            Long idDiagnosticosConsultas,
            Long idConsultorios,
            Long idConsultas
    );

    // Lista diagnósticos activos de una consulta.
    List<DiagnosticoConsulta>
    findAllByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstadoOrderByPrincipalDescFechaRegistroAscIdDiagnosticosConsultasAsc(
            Long idConsultorios,
            Long idConsultas,
            EstadoDiagnostico estado
    );

    // Lista todos los diagnósticos, incluidos los inactivos.
    List<DiagnosticoConsulta>
    findAllByConsultorioIdConsultoriosAndConsultaIdConsultasOrderByPrincipalDescFechaRegistroAscIdDiagnosticosConsultasAsc(
            Long idConsultorios,
            Long idConsultas
    );

    // Obtiene el diagnóstico principal activo de una consulta.
    Optional<DiagnosticoConsulta>
    findFirstByConsultorioIdConsultoriosAndConsultaIdConsultasAndPrincipalTrueAndEstado(
            Long idConsultorios,
            Long idConsultas,
            EstadoDiagnostico estado
    );

    // Comprueba si existe un diagnóstico principal activo.
    boolean
    existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndPrincipalTrueAndEstado(
            Long idConsultorios,
            Long idConsultas,
            EstadoDiagnostico estado
    );

    // Comprueba si el código CIE-10 ya está activo en la consulta.
    boolean
    existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndCodigoCie10AndEstado(
            Long idConsultorios,
            Long idConsultas,
            String codigoCie10,
            EstadoDiagnostico estado
    );

    // Comprueba un CIE-10 duplicado excluyendo el diagnóstico editado.
    boolean
    existsByConsultorioIdConsultoriosAndConsultaIdConsultasAndCodigoCie10AndEstadoAndIdDiagnosticosConsultasNot(
            Long idConsultorios,
            Long idConsultas,
            String codigoCie10,
            EstadoDiagnostico estado,
            Long idDiagnosticosConsultas
    );

    // Lista la evolución diagnóstica completa del paciente.
    List<DiagnosticoConsulta>
    findAllByConsultorioIdConsultoriosAndConsultaPacienteIdPacientesAndEstadoOrderByFechaRegistroDescIdDiagnosticosConsultasDesc(
            Long idConsultorios,
            Long idPacientes,
            EstadoDiagnostico estado
    );

    // Cuenta los diagnósticos activos de una consulta.
    long
    countByConsultorioIdConsultoriosAndConsultaIdConsultasAndEstado(
            Long idConsultorios,
            Long idConsultas,
            EstadoDiagnostico estado
    );
}