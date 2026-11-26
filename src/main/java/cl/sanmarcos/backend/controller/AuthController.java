package cl.sanmarcos.backend.controller;

import cl.sanmarcos.backend.model.Usuario;
import cl.sanmarcos.backend.security.JwtUtil;
import cl.sanmarcos.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Endpoints:
// POST /api/v1/auth/login    -> recibe correo y contrasena, devuelve el token JWT
// POST /api/v1/auth/renovar  -> recibe un token valido, devuelve uno nuevo (renovacion)
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y renovación del token JWT")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Body: { \"email\": \"...\", \"password\": \"...\" }. Devuelve el token y los datos del usuario.")
    public Map<String, Object> login(@RequestBody Map<String, String> datos) {
        Usuario usuario = usuarioService.login(datos.get("email"), datos.get("password"));
        String token = jwtUtil.generarToken(usuario);
        return Map.of("token", token, "usuario", usuario);
    }

    @PostMapping("/renovar")
    @Operation(summary = "Renovar el token", description = "Envía un token que todavía no vence y recibe uno nuevo con más tiempo.")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, Object> renovar(Authentication autenticacion) {
        // JwtFiltro ya valido el token y dejo el usuario en "autenticacion".
        Usuario usuario = (Usuario) autenticacion.getPrincipal();
        String token = jwtUtil.generarToken(usuario);
        return Map.of("token", token, "usuario", usuario);
    }
}
