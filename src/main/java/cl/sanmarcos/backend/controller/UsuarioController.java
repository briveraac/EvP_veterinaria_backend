package cl.sanmarcos.backend.controller;

import cl.sanmarcos.backend.model.Usuario;
import cl.sanmarcos.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints (solo Administrador, ver SecurityConfig):
// GET  /api/v1/usuarios       -> lista los usuarios
// GET  /api/v1/usuarios/{id}  -> busca un usuario
// POST /api/v1/usuarios       -> crea un usuario
// PUT  /api/v1/usuarios/{id}  -> edita un usuario (tambien sirve para activar/desactivar)
// No hay DELETE: los usuarios se desactivan para no perder el historial.
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios y roles (solo Administrador)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<Usuario> listar() {
        return usuarioService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un usuario por id")
    public Usuario buscar(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario", description = "La contraseña es obligatoria (mínimo 6 caracteres).")
    public Usuario crear(@RequestBody Usuario usuario) {
        return usuarioService.crear(usuario);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar usuario", description = "Si la contraseña viene vacía, se mantiene la actual.")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.actualizar(id, usuario);
    }
}
