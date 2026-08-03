package com.zenticode.medical.antecedentes.repository;

import com.zenticode.medical.antecedentes.entity.AntecedenteClinico;
import com.zenticode.medical.antecedentes.entity.AntecedenteClinico.TipoAntecedente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso controlado a antecedentes clínicos.
 */
public interface AntecedenteClinicoRepository
        extends JpaRepository<AntecedenteClinico, Long> {

    // Lista los antecedentes activos de una historia.
    @Query("""
            SELECT antecedente
            FROM AntecedenteClinico antecedente
            JOIN FETCH antecedente.historiaClinica historia
            JOIN FETCH historia.consultorio consultorio
            JOIN FETCH historia.paciente paciente
            WHERE consultorio.idConsultorios = :idConsultorios
              AND paciente.idPacientes = :idPacientes
              AND historia.idHistoriasClinicas =
                  :idHistoriasClinicas
              AND antecedente.activo = true
            ORDER BY
                antecedente.importancia DESC,
                antecedente.fechaCreacion DESC,
                antecedente.idAntecedentesClinicos DESC
            """)
    List<AntecedenteClinico> listarActivos(
            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idPacientes")
            Long idPacientes,

            @Param("idHistoriasClinicas")
            Long idHistoriasClinicas
    );

    // Lista los antecedentes activos de un tipo.
    @Query("""
            SELECT antecedente
            FROM AntecedenteClinico antecedente
            JOIN FETCH antecedente.historiaClinica historia
            JOIN FETCH historia.consultorio consultorio
            JOIN FETCH historia.paciente paciente
            WHERE consultorio.idConsultorios = :idConsultorios
              AND paciente.idPacientes = :idPacientes
              AND historia.idHistoriasClinicas =
                  :idHistoriasClinicas
              AND antecedente.tipo = :tipo
              AND antecedente.activo = true
            ORDER BY
                antecedente.importancia DESC,
                antecedente.fechaCreacion DESC,
                antecedente.idAntecedentesClinicos DESC
            """)
    List<AntecedenteClinico> listarActivosPorTipo(
            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idPacientes")
            Long idPacientes,

            @Param("idHistoriasClinicas")
            Long idHistoriasClinicas,

            @Param("tipo")
            TipoAntecedente tipo
    );

    // Busca un antecedente activo dentro de su contexto.
    @Query("""
            SELECT antecedente
            FROM AntecedenteClinico antecedente
            JOIN FETCH antecedente.historiaClinica historia
            JOIN FETCH historia.consultorio consultorio
            JOIN FETCH historia.paciente paciente
            WHERE antecedente.idAntecedentesClinicos =
                  :idAntecedentesClinicos
              AND consultorio.idConsultorios =
                  :idConsultorios
              AND paciente.idPacientes =
                  :idPacientes
              AND historia.idHistoriasClinicas =
                  :idHistoriasClinicas
              AND antecedente.activo = true
            """)
    Optional<AntecedenteClinico> buscarActivoPorId(
            @Param("idAntecedentesClinicos")
            Long idAntecedentesClinicos,

            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idPacientes")
            Long idPacientes,

            @Param("idHistoriasClinicas")
            Long idHistoriasClinicas
    );

    // Busca un antecedente aunque esté desactivado.
    @Query("""
            SELECT antecedente
            FROM AntecedenteClinico antecedente
            JOIN FETCH antecedente.historiaClinica historia
            JOIN FETCH historia.consultorio consultorio
            JOIN FETCH historia.paciente paciente
            WHERE antecedente.idAntecedentesClinicos =
                  :idAntecedentesClinicos
              AND consultorio.idConsultorios =
                  :idConsultorios
              AND paciente.idPacientes =
                  :idPacientes
              AND historia.idHistoriasClinicas =
                  :idHistoriasClinicas
            """)
    Optional<AntecedenteClinico> buscarPorId(
            @Param("idAntecedentesClinicos")
            Long idAntecedentesClinicos,

            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idPacientes")
            Long idPacientes,

            @Param("idHistoriasClinicas")
            Long idHistoriasClinicas
    );

    // Cuenta los antecedentes activos de una historia.
    @Query("""
            SELECT COUNT(antecedente)
            FROM AntecedenteClinico antecedente
            JOIN antecedente.historiaClinica historia
            JOIN historia.consultorio consultorio
            JOIN historia.paciente paciente
            WHERE consultorio.idConsultorios = :idConsultorios
              AND paciente.idPacientes = :idPacientes
              AND historia.idHistoriasClinicas =
                  :idHistoriasClinicas
              AND antecedente.activo = true
            """)
    long contarActivos(
            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idPacientes")
            Long idPacientes,

            @Param("idHistoriasClinicas")
            Long idHistoriasClinicas
    );

    // Comprueba la existencia de un antecedente activo.
    @Query("""
            SELECT CASE
                WHEN COUNT(antecedente) > 0
                    THEN true
                ELSE false
            END
            FROM AntecedenteClinico antecedente
            JOIN antecedente.historiaClinica historia
            JOIN historia.consultorio consultorio
            JOIN historia.paciente paciente
            WHERE antecedente.idAntecedentesClinicos =
                  :idAntecedentesClinicos
              AND consultorio.idConsultorios =
                  :idConsultorios
              AND paciente.idPacientes =
                  :idPacientes
              AND historia.idHistoriasClinicas =
                  :idHistoriasClinicas
              AND antecedente.activo = true
            """)
    boolean existeActivo(
            @Param("idAntecedentesClinicos")
            Long idAntecedentesClinicos,

            @Param("idConsultorios")
            Long idConsultorios,

            @Param("idPacientes")
            Long idPacientes,

            @Param("idHistoriasClinicas")
            Long idHistoriasClinicas
    );
}