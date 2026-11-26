package cl.sanmarcos.backend.config;

import cl.sanmarcos.backend.model.Usuario;
import cl.sanmarcos.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

// Crea los usuarios de ejemplo al iniciar, solo si la tabla esta vacia.
// Se hace en Java (y no en data.sql) porque las contrasenas se tienen que encriptar con BCrypt.
@Configuration
public class DatosIniciales {

    public static final String CLAVE_DEMO = "SanMarcos2026!";

    @Bean
    public CommandLineRunner crearUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            String clave = encoder.encode(CLAVE_DEMO);
            repo.save(new Usuario("Bruno Rivera", "brun.rivera@duocuc.cl", clave, Usuario.ADMIN));
            repo.save(new Usuario("German Pino", "germ.pino@duocuc.cl", clave, Usuario.ADMIN));
            repo.save(new Usuario("Recepción San Marcos", "recepcion@sanmarcos.cl", clave, Usuario.RECEPCIONISTA));
            repo.save(new Usuario("Cliente de demostración", "cliente@sanmarcos.cl", clave, Usuario.CLIENTE));
            System.out.println("Usuarios de ejemplo creados. Contraseña: " + CLAVE_DEMO);
        };
    }
}
