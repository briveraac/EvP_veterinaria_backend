package cl.sanmarcos.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Atrapa las excepciones de todos los controladores y responde con un JSON:
// { "mensaje": "..." }
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(CitaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> citaNoEncontrada(CitaNoEncontradaException error) {
        return Map.of("mensaje", error.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> datosInvalidos(IllegalArgumentException error) {
        return Map.of("mensaje", error.getMessage());
    }
}
