package cl.sanmarcos.backend.exception;

// Se lanza cuando se busca una cita con un id que no existe.
public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(Long id) {
        super("No existe una cita con id " + id + ".");
    }
}
