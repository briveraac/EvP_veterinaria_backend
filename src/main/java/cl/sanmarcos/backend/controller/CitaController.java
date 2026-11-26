package cl.sanmarcos.backend.controller;

import cl.sanmarcos.backend.model.Cita;
import cl.sanmarcos.backend.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

// Endpoints (necesitan token, ver SecurityConfig):
// GET    /api/v1/citas       -> lista todas las citas        (Administrador, Recepcionista)
// GET    /api/v1/citas/{id}  -> busca una cita               (Administrador, Recepcionista)
// POST   /api/v1/citas       -> crea una cita                (Administrador, Recepcionista)
// PUT    /api/v1/citas/{id}  -> actualiza una cita           (Administrador, Recepcionista)
// DELETE /api/v1/citas/{id}  -> elimina una cita             (solo Administrador)
@RestController
@RequestMapping("/api/v1/citas")
@Tag(name = "Citas", description = "CRUD de las citas de la veterinaria")
@SecurityRequirement(name = "bearerAuth")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    @Operation(summary = "Listar todas las citas")
    public List<Cita> listar() {
        return citaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar una cita por id")
    @ApiResponse(responseCode = "404", description = "La cita no existe")
    public Cita buscar(@PathVariable Long id) {
        return citaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una cita", description = "Valida datos obligatorios y que el veterinario no tenga otra cita a la misma hora.")
    @ApiResponse(responseCode = "201", description = "Cita creada")
    @ApiResponse(responseCode = "400", description = "Faltan datos o hay choque de horario")
    public Cita crear(@RequestBody Cita cita) {
        return citaService.crear(cita);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una cita")
    @ApiResponse(responseCode = "400", description = "Faltan datos o hay choque de horario")
    @ApiResponse(responseCode = "404", description = "La cita no existe")
    public Cita actualizar(@PathVariable Long id, @RequestBody Cita cita) {
        return citaService.actualizar(id, cita);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una cita (solo Administrador)")
    @ApiResponse(responseCode = "204", description = "Cita eliminada")
    @ApiResponse(responseCode = "403", description = "El usuario no es Administrador")
    public void eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
    }
}
