package cl.sanmarcos.backend.service;

import cl.sanmarcos.backend.exception.CredencialesInvalidasException;
import cl.sanmarcos.backend.exception.UsuarioNoEncontradoException;
import cl.sanmarcos.backend.model.Usuario;
import cl.sanmarcos.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Logica de negocio de los usuarios: login, crear, editar y activar/desactivar.
@Service
public class UsuarioService {

    private static final List<String> ROLES = List.of(Usuario.ADMIN, Usuario.RECEPCIONISTA, Usuario.CLIENTE);

    @Autowired
    private UsuarioRepository usuarioRepository;

    // BCrypt: encripta las contrasenas (se define en SecurityConfig).
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Revisa correo y contrasena. Si estan bien devuelve el usuario.
    public Usuario login(String email, String password) {
        if (email == null || password == null) {
            throw new CredencialesInvalidasException();
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (usuario == null || !usuario.isActive()) {
            throw new CredencialesInvalidasException();
        }
        // matches compara la clave escrita con la clave encriptada guardada.
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    public Usuario crear(Usuario usuario) {
        usuario.setId(null);
        validar(usuario);
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario existente = obtenerPorId(id);
        datos.setId(id);
        validar(datos);

        existente.setName(datos.getName().trim());
        existente.setEmail(datos.getEmail().trim().toLowerCase());
        existente.setRole(datos.getRole());
        existente.setActive(datos.isActive());

        // Si no mandan contrasena nueva, se mantiene la anterior.
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            if (datos.getPassword().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
            }
            existente.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        return usuarioRepository.save(existente);
    }

    private void validar(Usuario usuario) {
        if (usuario.getName() == null || usuario.getName().trim().length() < 3) {
            throw new IllegalArgumentException("Ingresa un nombre de al menos 3 caracteres.");
        }
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("Ingresa un correo electrónico válido.");
        }
        if (!ROLES.contains(usuario.getRole())) {
            throw new IllegalArgumentException("El rol no es válido.");
        }

        boolean hayOtroAdmin = false;
        for (Usuario otro : usuarioRepository.findAll()) {
            if (otro.getId().equals(usuario.getId())) {
                continue;
            }
            // No se permiten dos usuarios con el mismo correo.
            if (otro.getEmail().equalsIgnoreCase(usuario.getEmail().trim())) {
                throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico.");
            }
            if (otro.isActive() && otro.getRole().equals(Usuario.ADMIN)) {
                hayOtroAdmin = true;
            }
        }

        // Siempre debe quedar al menos un administrador activo.
        boolean esteEsAdminActivo = usuario.isActive() && usuario.getRole().equals(Usuario.ADMIN);
        if (!hayOtroAdmin && !esteEsAdminActivo) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo en el sistema.");
        }
    }
}
