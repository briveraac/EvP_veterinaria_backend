package cl.sanmarcos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void crearCitaYDetectarChoqueDeHorario() throws Exception {
        String token = login("brun.rivera@duocuc.cl");
        String cita = "{\"pet\":\"Toby\",\"species\":\"Perro\",\"owner\":\"Ana Rojas\",\"service\":\"Consulta general\","
                + "\"date\":\"2099-01-01\",\"time\":\"10:00\",\"vet\":\"Dr. Nicolás Vega\"}";

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
