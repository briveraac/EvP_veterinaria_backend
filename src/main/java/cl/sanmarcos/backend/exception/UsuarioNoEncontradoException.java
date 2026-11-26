package cl.sanmarcos.backend.exception;

// Se lanza cuando se busca un usuario con un id que no existe.
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe un usuario con id " + id + ".");
    }
}
