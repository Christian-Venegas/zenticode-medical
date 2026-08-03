package com.zenticode.medical;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prueba de integración básica del contexto completo de la aplicación.
 *
 * <p>Esta prueba comprueba que Spring Boot puede construir correctamente
 * todos los componentes principales del backend:</p>
 *
 * <ul>
 *     <li>Configuración de seguridad.</li>
 *     <li>Entidades JPA.</li>
 *     <li>Repositorios.</li>
 *     <li>Servicios.</li>
 *     <li>Controladores.</li>
 *     <li>Flyway.</li>
 *     <li>PostgreSQL mediante Testcontainers.</li>
 * </ul>
 *
 * <p>A diferencia de las pruebas unitarias, esta prueba necesita un entorno
 * Docker compatible porque {@link TestcontainersConfiguration} inicia una
 * instancia temporal y aislada de PostgreSQL.</p>
 *
 * <p>La opción {@code disabledWithoutDocker = true} permite que:</p>
 *
 * <ul>
 *     <li>La prueba se ejecute cuando Docker está disponible.</li>
 *     <li>La prueba se marque como omitida cuando Docker no está activo.</li>
 *     <li>Las pruebas unitarias continúen ejecutándose normalmente.</li>
 *     <li>La ausencia de Docker no se confunda con un error del código.</li>
 * </ul>
 *
 * <p>Esta configuración no oculta fallos cuando Docker sí está disponible.
 * Si el contenedor PostgreSQL inicia y Flyway, Hibernate o Spring fallan,
 * la prueba seguirá produciendo un error real.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class MedicalApiApplicationTests {

	/**
	 * Comprueba que el contexto completo de Spring Boot pueda iniciar.
	 *
	 * <p>El método no necesita instrucciones internas. La prueba consiste
	 * precisamente en que Spring consiga construir el contexto antes de
	 * ejecutar este método.</p>
	 *
	 * <p>Si existe Docker, el flujo será:</p>
	 *
	 * <pre>
	 * JUnit
	 *   ↓
	 * Testcontainers
	 *   ↓
	 * PostgreSQL temporal
	 *   ↓
	 * Flyway V1
	 *   ↓
	 * Hibernate validate
	 *   ↓
	 * Contexto Spring Boot
	 * </pre>
	 */
	@Test
	void contextLoads() {
		/*
		 * El cuerpo queda vacío intencionalmente.
		 *
		 * Si Spring Boot alcanza este punto, significa que el contexto fue
		 * construido correctamente.
		 */
	}
}