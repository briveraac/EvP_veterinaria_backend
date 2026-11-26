package cl.sanmarcos.backend.repository;

import cl.sanmarcos.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring arma la consulta solo con el nombre del metodo:
    // SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(?)
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
