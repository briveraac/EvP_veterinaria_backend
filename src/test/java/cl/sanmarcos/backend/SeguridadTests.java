package cl.sanmarcos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas de integracion: levantan la aplicacion completa (con H2)
// y hacen peticiones HTTP de verdad contra los endpoints.
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTests {

    @Autowired
    private MockMvc mockMvc;

    // Hace login y devuelve solo el token.
    private String login(String email) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"SanMarcos2026!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // La respuesta es {"token":"xxx", ...}: sacamos lo que esta entre comillas.
        int inicio = respuesta.indexOf("\"token\":\"") + 9;
        int fin = respuesta.indexOf("\"", inicio);
        return respuesta.substring(inicio, fin);
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/citas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void claveIncorrectaResponde401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"brun.rivera@duocuc.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFalsoResponde401() throws Exception {
        mockMvc.perform(get("/api/v1/citas").header("Authorization", "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPuedeVerCitasYUsuarios() throws Exception {
        String token = login("brun.rivera@duocuc.cl");
        mockMvc.perform(get("/api/v1/citas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // La contrasena nunca se envia en el JSON.
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void recepcionistaNoPuedeEliminarNiVerUsuarios() throws Exception {
        String token = login("recepcion@sanmarcos.cl");
        mockMvc.perform(get("/api/v1/citas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/citas/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeVerLasCitasDeLaClinica() throws Exception {
        String token = login("cliente@sanmarcos.cl");
        mockMvc.perform(get("/api/v1/citas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // Proxima fecha que cae en ese dia de la semana (siempre en el futuro).
    private String proximo(DayOfWeek dia) {
        return LocalDate.now(ZoneId.of("America/Santiago")).plusWeeks(1).with(TemporalAdjusters.nextOrSame(dia)).toString();
    }

    private String citaJson(String fecha, String hora) {
        return "{\"pet\":\"Toby\",\"species\":\"Perro\",\"owner\":\"Ana Rojas\",\"service\":\"Consulta general\","
                + "\"date\":\"" + fecha + "\",\"time\":\"" + hora + "\",\"vet\":\"Dra. Paula Fuentes\"}";
    }

    @Test
    void noPermiteFechasPasadasNiFueraDeHorario() throws Exception {
        String token = login("brun.rivera@duocuc.cl");
        String ayer = LocalDate.now(ZoneId.of("America/Santiago")).minusDays(1).toString();

        // Fecha pasada
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(citaJson(ayer, "10:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede agendar una cita en una fecha u hora que ya pasó."));

        // Domingo
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(citaJson(proximo(DayOfWeek.SUNDAY), "11:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Los domingos la clínica está cerrada."));

        // Lunes a las 20:00 (cierra 19:30) y a las 08:00 (abre 09:00)
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(citaJson(proximo(DayOfWeek.MONDAY), "20:00")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(citaJson(proximo(DayOfWeek.MONDAY), "08:00")))
                .andExpect(status().isBadRequest());

        // Sabado a las 16:00 (cierra 15:00)
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(citaJson(proximo(DayOfWeek.SATURDAY), "16:00")))
                .andExpect(status().isBadRequest());

        // Sabado a las 10:00 si se puede
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(citaJson(proximo(DayOfWeek.SATURDAY), "10:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void sePuedeCompletarUnaCitaSinCambiarSuFecha() throws Exception {
        // La cita 1 de data.sql es de hoy a las 09:00 (puede que ya haya pasado).
        // Cambiar solo el estado no debe revisar el horario.
        String token = login("brun.rivera@duocuc.cl");
        String fecha = LocalDate.now().toString();
        String cita = "{\"pet\":\"Luna\",\"species\":\"Perro\",\"owner\":\"Francisca Muñoz\",\"service\":\"Consulta general\","
                + "\"date\":\"" + fecha + "\",\"time\":\"09:00\",\"vet\":\"Dra. Sofía Herrera\",\"status\":\"Completada\"}";
        mockMvc.perform(put("/api/v1/citas/1").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cita))
                .andExpect(status().isOk());
    }

    @Test
    void crearCitaYDetectarChoqueDeHorario() throws Exception {
        String token = login("brun.rivera@duocuc.cl");
        String cita = "{\"pet\":\"Toby\",\"species\":\"Perro\",\"owner\":\"Ana Rojas\",\"service\":\"Consulta general\","
                + "\"date\":\"" + proximo(DayOfWeek.TUESDAY) + "\",\"time\":\"10:00\",\"vet\":\"Dr. Nicolás Vega\"}";

        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cita))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Pendiente"));

        // Misma fecha, hora y veterinario: debe rechazarla.
        mockMvc.perform(post("/api/v1/citas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(cita))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El veterinario ya tiene una cita en esa fecha y hora."));
    }

    @Test
    void renovarEntregaUnTokenNuevo() throws Exception {
        String token = login("brun.rivera@duocuc.cl");
        mockMvc.perform(post("/api/v1/auth/renovar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
