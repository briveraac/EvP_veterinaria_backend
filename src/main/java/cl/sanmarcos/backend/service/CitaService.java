package cl.sanmarcos.backend.service;

import cl.sanmarcos.backend.exception.CitaNoEncontradaException;
import cl.sanmarcos.backend.model.Cita;
import cl.sanmarcos.backend.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Aquí va la lógica de negocio. El controlador solo recibe y responde.
@Service
public class CitaService {

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
        return citaRepository.save(cita);
    }

    public Cita actualizar(Long id, Cita datos) {
        Cita existente = obtenerPorId(id);
        datos.setId(id);
        validar(datos);

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

    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
