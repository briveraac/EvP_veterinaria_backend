package cl.sanmarcos.backend.config;

import cl.sanmarcos.backend.model.Usuario;
import cl.sanmarcos.backend.repository.UsuarioRepository;
import cl.sanmarcos.backend.security.JwtFiltro;
import cl.sanmarcos.backend.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Aqui se define que rutas son publicas y que rol necesita cada una.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil, UsuarioRepository usuarioRepository)
            throws Exception {

        http
                // Usamos el CorsConfigurationSource de CorsConfig.
                .cors(Customizer.withDefaults())
                // CSRF se desactiva porque no usamos cookies de sesion: el token va en la cabecera.
                .csrf(csrf -> csrf.disable())
                // STATELESS: el servidor no guarda sesiones, cada peticion trae su token.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola de H2 usa frames.
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(rutas -> rutas
                        // Publico: login, Swagger, consola H2 y pagina de error.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Usuarios: solo el administrador.
                        .requestMatchers("/api/v1/usuarios/**").hasAuthority(Usuario.ADMIN)

                        // Citas: eliminar solo el administrador.
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/citas/**").hasAuthority(Usuario.ADMIN)
                        // Ver, crear y editar: administrador o recepcionista.
                        .requestMatchers("/api/v1/citas/**").hasAnyAuthority(Usuario.ADMIN, Usuario.RECEPCIONISTA)

                        // Todo lo demas (por ejemplo /api/v1/auth/renovar) pide estar logueado.
                        .anyRequest().authenticated())
                // Respuestas en JSON cuando falta el token (401) o falta permiso (403).
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"mensaje\":\"Debes iniciar sesión.\"}");
                        })
                        .accessDeniedHandler((request, response, error) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"mensaje\":\"No tienes permiso para hacer esto.\"}");
                        }))
                // Nuestro filtro JWT corre antes del filtro de login de Spring.
                .addFilterBefore(new JwtFiltro(jwtUtil, usuarioRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt encripta las contrasenas con "sal": dos claves iguales dan textos distintos.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
