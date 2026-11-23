# Veterinaria San Marcos - Backend

API REST hecha con Spring Boot para las citas de la veterinaria.

El frontend (React + Vite) está en otro repositorio: `EvP_veterinaria`. Se conectan por HTTP: el frontend llama a `http://localhost:8080/api/v1/citas`.

## Requisitos

- Java JDK 17 o superior.
- No hace falta instalar Maven: el proyecto trae `mvnw`.

## Cómo ejecutarlo

En Windows:

```
mvnw.cmd spring-boot:run
```

En Mac o Linux:

```
./mvnw spring-boot:run
```

También se puede ejecutar la clase `BackendApplication` desde IntelliJ o VS Code.

El servidor queda en `http://localhost:8080`.

## Endpoints

| Método | Ruta | Qué hace |
| --- | --- | --- |
| GET | `/api/v1/citas` | Lista todas las citas |
| GET | `/api/v1/citas/{id}` | Busca una cita |
| POST | `/api/v1/citas` | Crea una cita |
| PUT | `/api/v1/citas/{id}` | Actualiza una cita |
| DELETE | `/api/v1/citas/{id}` | Elimina una cita |

Ejemplo de cita en JSON:

```json
{
  "pet": "Luna",
  "species": "Perro",
  "owner": "Francisca Muñoz",
  "service": "Consulta general",
  "date": "2026-11-05",
  "time": "09:00",
  "vet": "Dra. Sofía Herrera",
  "status": "Pendiente"
}
```

Si hay un error, la respuesta tiene el formato `{ "mensaje": "..." }`.

## Estructura

- `model/Cita.java`: entidad JPA (tabla `citas`).
- `repository/CitaRepository.java`: acceso a la base de datos con `JpaRepository`.
- `service/CitaService.java`: lógica de negocio (datos obligatorios y que un veterinario no tenga dos citas a la misma hora).
- `controller/CitaController.java`: endpoints REST.
- `exception/`: excepción personalizada y manejo de errores con `@RestControllerAdvice`.
- `config/CorsConfig.java`: permite las llamadas desde el frontend (`localhost:5173` y `localhost:4173`).

## Base de datos

Se usa H2 en memoria. Al iniciar se cargan citas de ejemplo desde `src/main/resources/data.sql`. Los datos se borran al detener el servidor.

Consola de H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:veterinaria`, usuario `sa`, sin contraseña).

## Ejecutar con Docker

Requisitos: Docker Desktop (o Docker + Docker Compose).

```
docker compose up --build
```

Esto levanta dos contenedores: `db` (PostgreSQL 16) y `backend` (esta API). La API queda en `http://localhost:8080`. Dentro de Docker el backend se conecta a la base con el nombre del servicio (`db`), no con `localhost`.

- Los datos se guardan en el volumen `db_data`, así que no se pierden al reiniciar.
- Para apagar: `docker compose down` (agrega `-v` para borrar también los datos).
- Se usa el perfil `docker` (`application-docker.properties`) y `data-postgres.sql`. Sin Docker, el proyecto sigue funcionando igual con H2.
