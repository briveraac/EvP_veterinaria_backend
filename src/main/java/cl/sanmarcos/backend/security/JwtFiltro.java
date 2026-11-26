package cl.sanmarcos.backend.security;

import cl.sanmarcos.backend.model.Usuario;
import cl.sanmarcos.backend.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Se ejecuta UNA vez en cada peticion, antes de llegar al controlador.
// Busca la cabecera "Authorization: Bearer <token>".
// Si el token es valido, le dice a Spring Security quien es el usuario y que rol tiene.
// Si no hay token o es invalido, no hace nada: Spring Security respondera 401 si la ruta lo exige.
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public JwtFiltro(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String token = cabecera.substring(7); // quitamos "Bearer "
            try {
                String email = jwtUtil.obtenerEmail(token);

                // Volvemos a buscar el usuario en la BD: si lo desactivaron o le
                // cambiaron el rol, el cambio se aplica de inmediato.
                Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
                if (usuario != null && usuario.isActive()) {
                    // El rol se usa como "authority" (permiso) en Spring Security.
                    List<SimpleGrantedAuthority> permisos = List.of(new SimpleGrantedAuthority(usuario.getRole()));
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(usuario, null, permisos);
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token falso, mal formado o vencido: seguimos sin usuario.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
