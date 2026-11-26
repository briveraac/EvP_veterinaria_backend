package cl.sanmarcos.backend.security;

import cl.sanmarcos.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Crea y valida los tokens JWT.
// Un JWT tiene 3 partes separadas por puntos: header.payload.firma
// La firma se hace con una clave secreta que solo conoce el backend.
// Si alguien cambia el payload (por ejemplo el rol), la firma ya no coincide.
@Component
public class JwtUtil {

    private final SecretKey clave;
    private final long duracionMinutos;

    // Los valores vienen de application.properties (o de variables de entorno en Docker).
    public JwtUtil(@Value("${jwt.secreto}") String secreto,
                   @Value("${jwt.duracion-minutos}") long duracionMinutos) {
        // HS256 necesita una clave de al menos 32 caracteres.
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionMinutos = duracionMinutos;
    }

    // Genera un token nuevo para el usuario.
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date vence = new Date(ahora.getTime() + duracionMinutos * 60 * 1000);

        return Jwts.builder()
                .subject(usuario.getEmail())        // a quien pertenece el token
                .claim("rol", usuario.getRole())    // rol del usuario
                .claim("nombre", usuario.getName())
                .issuedAt(ahora)                    // cuando se creo
                .expiration(vence)                  // cuando vence
                .signWith(clave)                    // firma HS256
                .compact();
    }

    // Revisa la firma y la fecha de vencimiento.
    // Si el token es falso o esta vencido, lanza una excepcion (JwtException).
    public Claims leerToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String obtenerEmail(String token) {
        return leerToken(token).getSubject();
    }
}
