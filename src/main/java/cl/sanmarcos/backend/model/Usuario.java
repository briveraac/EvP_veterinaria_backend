package cl.sanmarcos.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Cada objeto Usuario es una fila de la tabla "usuarios".
// Igual que en Cita, los atributos usan los nombres del frontend (name, email, role, active).
@Entity
@Table(name = "usuarios")
public class Usuario {

    // Roles que existen en el sistema (los mismos textos que usa el frontend).
    public static final String ADMIN = "Administrador";
    public static final String RECEPCIONISTA = "Recepcionista";
    public static final String CLIENTE = "Dueño de mascota";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "correo", unique = true)
    private String email;

    // Se guarda encriptada con BCrypt, nunca en texto plano.
    // WRITE_ONLY: se puede recibir en el JSON, pero nunca se envia al frontend.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "clave")
    private String password;

    @Column(name = "rol")
    private String role;

    @Column(name = "activo")
    private boolean active = true;

    public Usuario() {
    }

    public Usuario(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
