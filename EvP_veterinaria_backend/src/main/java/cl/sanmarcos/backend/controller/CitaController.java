package cl.sanmarcos.backend.controller;

import cl.sanmarcos.backend.model.Cita;
import cl.sanmarcos.backend.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints:
// GET    /api/citas       -> lista todas las citas
// GET    /api/citas/{id}  -> busca una cita
// POST   /api/citas       -> crea una cita
// PUT    /api/citas/{id}  -> actualiza una cita
// DELETE /api/citas/{id}  -> elimina una cita
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    public List<Cita> listar() {
        return citaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public Cita buscar(@PathVariable Long id) {
        return citaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cita crear(@RequestBody Cita cita) {
        return citaService.crear(cita);
    }

    @PutMapping("/{id}")
    public Cita actualizar(@PathVariable Long id, @RequestBody Cita cita) {
        return citaService.actualizar(id, cita);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
    }
}
