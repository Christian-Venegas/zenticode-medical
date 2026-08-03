package com.zenticode.medical.consultorios.repository;

import com.zenticode.medical.consultorios.entity.Consultorio;
import com.zenticode.medical.consultorios.entity.Consultorio.EstadoConsultorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para la entidad {@link Consultorio}.
 *
 * <p>Esta interfaz actúa como frontera entre el dominio de consultorios y
 * PostgreSQL. Spring Data JPA crea automáticamente la implementación durante
 * el arranque de la aplicación.</p>
 *
 * <p>El repositorio ofrece las operaciones básicas heredadas de
 * {@link JpaRepository}, entre ellas:</p>
 *
 * <ul>
 *     <li>Guardar un consultorio.</li>
 *     <li>Buscar un consultorio por su clave primaria.</li>
 *     <li>Comprobar si existe un registro.</li>
 *     <li>Listar registros.</li>
 *     <li>Contar registros.</li>
 * </ul>
 *
 * <p>Además, se declaran consultas específicas utilizando nombres de métodos
 * interpretados por Spring Data JPA. Estas consultas no requieren escribir
 * JPQL ni SQL manual.</p>
 *
 * <p>Este repositorio no debe exponerse directamente desde un controlador.
 * Los controladores se comunicarán con una capa de servicio, donde se
 * aplicarán las reglas de negocio, transacciones, permisos y validaciones.</p>
 */
@Repository
public interface ConsultorioRepository
        extends JpaRepository<Consultorio, Long> {

    /**
     * Busca un consultorio utilizando su código público.
     *
     * <p>El código público permite identificar un consultorio sin exponer la
     * clave primaria secuencial {@code id_consultorios}. La base de datos
     * garantiza que {@code codigo_publico} sea único.</p>
     *
     * <p>Se utiliza {@link Optional} porque el código solicitado podría no
     * corresponder con ningún consultorio registrado. De esta forma se evita
     * devolver {@code null} y se obliga a la capa de servicio a gestionar
     * explícitamente la ausencia del registro.</p>
     *
     * @param codigoPublico código público que identifica el consultorio
     * @return consultorio encontrado o un {@link Optional} vacío
     */
    Optional<Consultorio> findByCodigoPublico(String codigoPublico);

    /**
     * Comprueba si ya existe un consultorio con un código público determinado.
     *
     * <p>Esta consulta se utilizará antes de guardar un nuevo consultorio,
     * como una validación comprensible para el usuario. No sustituye la
     * restricción única existente en PostgreSQL, que sigue siendo la última
     * barrera frente a condiciones de concurrencia.</p>
     *
     * @param codigoPublico código público que se desea comprobar
     * @return {@code true} si el código ya está registrado
     */
    boolean existsByCodigoPublico(String codigoPublico);

    /**
     * Comprueba si existe un RUC registrado.
     *
     * <p>El RUC es opcional en la primera versión porque el profesional está
     * empezando. Cuando se proporcione, esta consulta permitirá detectar una
     * posible repetición antes de guardar.</p>
     *
     * <p>Actualmente la migración no define el RUC como único. Por tanto, esta
     * operación sirve como apoyo a la regla de negocio, pero no debe tratarse
     * todavía como una garantía absoluta de unicidad.</p>
     *
     * @param ruc número de RUC que se desea comprobar
     * @return {@code true} si existe al menos un consultorio con ese RUC
     */
    boolean existsByRuc(String ruc);

    /**
     * Obtiene los consultorios que tienen un estado determinado.
     *
     * <p>Esta operación será útil para tareas administrativas, por ejemplo
     * listar consultorios activos, suspendidos o inactivos.</p>
     *
     * <p>No se expone todavía mediante una API porque la primera versión no
     * incluye un panel de superadministración. Se incorpora en el repositorio
     * porque representa una consulta coherente con el modelo actual.</p>
     *
     * @param estado estado operativo que se desea filtrar
     * @return lista de consultorios que coinciden con el estado
     */
    List<Consultorio> findAllByEstado(EstadoConsultorio estado);

    /**
     * Busca un consultorio por clave primaria y estado.
     *
     * <p>Esta consulta permitirá recuperar únicamente un consultorio que se
     * encuentre en el estado esperado. Por ejemplo, la autenticación futura
     * podrá verificar que el consultorio esté activo antes de permitir el
     * acceso de sus usuarios.</p>
     *
     * @param idConsultorios clave primaria del consultorio
     * @param estado estado requerido
     * @return consultorio coincidente o un {@link Optional} vacío
     */
    Optional<Consultorio> findByIdConsultoriosAndEstado(
            Long idConsultorios,
            EstadoConsultorio estado
    );

    /**
     * Comprueba si un consultorio concreto permanece activo.
     *
     * <p>La consulta puede utilizarse como comprobación ligera cuando no se
     * necesita cargar todos los datos de la entidad.</p>
     *
     * @param idConsultorios clave primaria del consultorio
     * @param estado estado que se desea comprobar
     * @return {@code true} si el consultorio existe en dicho estado
     */
    boolean existsByIdConsultoriosAndEstado(
            Long idConsultorios,
            EstadoConsultorio estado
    );
}