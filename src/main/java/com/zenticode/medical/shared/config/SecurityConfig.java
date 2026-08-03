package com.zenticode.medical.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura la seguridad HTTP y JWT de la API.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final int BCRYPT_STRENGTH = 12;

    private final boolean bootstrapConsultorioEnabled;
    private final boolean bootstrapUsuarioEnabled;

    // Recibe las propiedades temporales de inicialización.
    public SecurityConfig(
            @Value(
                    "${app.security."
                            + "bootstrap-consultorio-enabled:false}"
            )
            final boolean bootstrapConsultorioEnabled,

            @Value(
                    "${app.security."
                            + "bootstrap-usuario-enabled:false}"
            )
            final boolean bootstrapUsuarioEnabled
    ) {
        this.bootstrapConsultorioEnabled =
                bootstrapConsultorioEnabled;

        this.bootstrapUsuarioEnabled =
                bootstrapUsuarioEnabled;
    }

    // Define las reglas de acceso HTTP.
    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        // La API no utiliza sesiones HTTP.
        http.sessionManagement(session ->
                session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
        );

        // Desactiva mecanismos no utilizados por la API REST.
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);

        // Valida automáticamente los tokens Bearer.
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter
                        )
                )
        );

        http.authorizeHttpRequests(authorize -> {

            // Permite solicitudes OPTIONS del navegador.
            authorize.requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
            ).permitAll();

            // Permite únicamente el monitoreo público.
            authorize.requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info"
            ).permitAll();

            // Permite el login sin token previo.
            authorize.requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/login"
            ).permitAll();

            // Permite crear consultorios solo en bootstrap.
            if (bootstrapConsultorioEnabled) {
                authorize.requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/consultorios"
                ).permitAll();
            }

            // Permite probar usuarios solo en bootstrap.
            if (bootstrapUsuarioEnabled) {
                authorize.requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/consultorios/*/usuarios"
                ).permitAll();

                authorize.requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/consultorios/*/usuarios/*"
                ).permitAll();
            }

            // Exige un JWT válido para el resto de la API.
            authorize.requestMatchers(
                    "/api/v1/**"
            ).authenticated();

            // Bloquea cualquier ruta no declarada.
            authorize.anyRequest().denyAll();
        });

        return http.build();
    }

    // Convierte los roles del JWT en autoridades de Spring.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtGrantedAuthoritiesConverter rolesConverter =
                new JwtGrantedAuthoritiesConverter();

        // Lee la lista almacenada en el claim "roles".
        rolesConverter.setAuthoritiesClaimName("roles");

        // ADMIN_CONSULTORIO pasa a ROLE_ADMIN_CONSULTORIO.
        rolesConverter.setAuthorityPrefix("ROLE_");

        final JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                rolesConverter
        );

        // Utiliza idUsuarios como identidad autenticada.
        converter.setPrincipalClaimName("sub");

        return converter;
    }

    // Evita que Spring genere un usuario temporal.
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    // Genera hashes BCrypt para las contraseñas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(
                BCRYPT_STRENGTH
        );
    }
}

