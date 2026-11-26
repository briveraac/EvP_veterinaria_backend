package cl.sanmarcos.backend.exception;

// Se lanza cuando el correo o la contrasena no son correctos (responde 401).
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos, o cuenta inactiva.");
    }
}
