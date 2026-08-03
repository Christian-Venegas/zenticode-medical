package com.zenticode.medical.pacientes.repository;

import com.zenticode.medical.pacientes.entity.Paciente;
import com.zenticode.medical.pacientes.entity.Paciente.EstadoPaciente;
import com.zenticode.medical.pacientes.entity.Paciente.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de pacientes por consultorio.
 */
@Repository
public interface PacienteRepository
        extends JpaRepository<Paciente, Long> {

    // Busca un paciente dentro del consultorio propietario.
    Optional<Paciente>
    findByIdPacientesAndConsultorioIdConsultorios(
            Long idPacientes,
            Long idConsultorios
    );

    // Busca un paciente activo dentro del consultorio.
    Optional<Paciente>
    findByIdPacientesAndConsultorioIdConsultoriosAndEstado(
            Long idPacientes,
            Long idConsultorios,
            EstadoPaciente estado
    );

    // Comprueba documentos duplicados dentro del consultorio.
    boolean
    existsByConsultorioIdConsultoriosAndTipoDocumentoAndNumeroDocumento(
            Long idConsultorios,
            TipoDocumento tipoDocumento,
            String numeroDocumento
    );

    // Comprueba duplicados excluyendo al paciente editado.
    boolean
    existsByConsultorioIdConsultoriosAndTipoDocumentoAndNumeroDocumentoAndIdPacientesNot(
            Long idConsultorios,
            TipoDocumento tipoDocumento,
            String numeroDocumento,
            Long idPacientes
    );

    // Lista pacientes activos en orden alfabético.
    List<Paciente>
    findAllByConsultorioIdConsultoriosAndEstadoOrderByApellidosAscNombresAsc(
            Long idConsultorios,
            EstadoPaciente estado
    );

    // Busca pacientes activos por nombres.
    List<Paciente>
    findAllByConsultorioIdConsultoriosAndEstadoAndNombresContainingIgnoreCaseOrderByApellidosAscNombresAsc(
            Long idConsultorios,
            EstadoPaciente estado,
            String nombres
    );

    // Busca pacientes activos por apellidos.
    List<Paciente>
    findAllByConsultorioIdConsultoriosAndEstadoAndApellidosContainingIgnoreCaseOrderByApellidosAscNombresAsc(
            Long idConsultorios,
            EstadoPaciente estado,
            String apellidos
    );

    // Busca pacientes activos por número de documento.
    List<Paciente>
    findAllByConsultorioIdConsultoriosAndEstadoAndNumeroDocumentoContainingIgnoreCaseOrderByApellidosAscNombresAsc(
            Long idConsultorios,
            EstadoPaciente estado,
            String numeroDocumento
    );
}