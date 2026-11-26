package cl.sanmarcos.backend.service;

import cl.sanmarcos.backend.exception.CitaNoEncontradaException;
import cl.sanmarcos.backend.model.Cita;
import cl.sanmarcos.backend.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

// Aquí va la lógica de negocio. El controlador solo recibe y responde.
@Service
public class CitaService {

    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");

    @Autowired
    private CitaRepository citaRepository;

    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id));
    }

    public Cita crear(Cita cita) {
        cita.setId(null); // El id lo genera la base de datos.
        validar(cita);
        validarFechaYHorario(cita);
        return citaRepository.save(cita);
    }

    public Cita actualizar(Long id, Cita datos) {
        Cita existente = obtenerPorId(id);
        datos.setId(id);
        validar(datos);

        // Solo revisamos fecha y horario si los cambiaron (reagendar).
        // Así se puede seguir marcando como "Completada" una cita que ya pasó.
        boolean cambioFecha = !datos.getDate().equals(existente.getDate()) || !datos.getTime().equals(existente.getTime());
        if (cambioFecha) {
            validarFechaYHorario(datos);
        }

        existente.setPet(datos.getPet());
        existente.setSpecies(datos.getSpecies());
        existente.setOwner(datos.getOwner());
        existente.setService(datos.getService());
        existente.setDate(datos.getDate());
        existente.setTime(datos.getTime());
        existente.setVet(datos.getVet());
        existente.setStatus(datos.getStatus());
        return citaRepository.save(existente);
    }

    public void eliminar(Long id) {
        Cita existente = obtenerPorId(id);
        citaRepository.delete(existente);
    }

    private void validar(Cita cita) {
        if (estaVacio(cita.getPet()) || estaVacio(cita.getOwner()) || estaVacio(cita.getVet())
                || estaVacio(cita.getDate()) || estaVacio(cita.getTime())) {
            throw new IllegalArgumentException("Completa los datos de la cita.");
        }

        if (estaVacio(cita.getStatus())) {
            cita.setStatus("Pendiente");
        }

        // Un veterinario no puede tener dos citas activas a la misma hora.
        if (!cita.getStatus().equals("Cancelada")) {
            for (Cita otra : citaRepository.findAll()) {
                boolean esLaMisma = otra.getId().equals(cita.getId());
                boolean mismoHorario = otra.getDate().equals(cita.getDate()) && otra.getTime().equals(cita.getTime());
                boolean mismoVeterinario = otra.getVet().equals(cita.getVet());
                if (!esLaMisma && mismoHorario && mismoVeterinario && !otra.getStatus().equals("Cancelada")) {
                    throw new IllegalArgumentException("El veterinario ya tiene una cita en esa fecha y hora.");
                }
            }
        }
    }

    // Horario de atención de la clínica (el mismo que aparece en la página de contacto):
    // Lunes a viernes de 09:00 a 19:30, sábado de 10:00 a 15:00, domingo cerrado.
    private void validarFechaYHorario(Cita cita) {
        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(cita.getDate()); // formato "2026-11-05"
            hora = LocalTime.parse(cita.getTime());  // formato "09:00"
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha o la hora no tienen un formato válido.");
        }

        // Usamos la hora de Chile: el servidor (por ejemplo Docker o AWS) puede estar en otra zona horaria.
        LocalDate hoy = LocalDate.now(ZONA_CHILE);
        LocalTime ahora = LocalTime.now(ZONA_CHILE);

        // 1. No se puede agendar en el pasado.
        if (fecha.isBefore(hoy) || (fecha.equals(hoy) && hora.isBefore(ahora))) {
            throw new IllegalArgumentException("No se puede agendar una cita en una fecha u hora que ya pasó.");
        }

        // 2. Domingo cerrado.
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Los domingos la clínica está cerrada.");
        }

        // 3. Dentro del horario de ese día.
        LocalTime abre = LocalTime.of(9, 0);
        LocalTime cierra = LocalTime.of(19, 30);
        if (dia == DayOfWeek.SATURDAY) {
            abre = LocalTime.of(10, 0);
            cierra = LocalTime.of(15, 0);
        }
        if (hora.isBefore(abre) || !hora.isBefore(cierra)) {
            throw new IllegalArgumentException("Fuera del horario de atención. Lunes a viernes de 09:00 a 19:30 y sábado de 10:00 a 15:00.");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
