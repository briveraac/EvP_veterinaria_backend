# Veterinaria San Marcos - Backend

API REST hecha con Spring Boot para las citas y usuarios de la veterinaria.
Tiene autenticación con **JWT** y permisos según el **rol** del usuario.

El frontend (React + Vite) está en otro repositorio: `EvP_veterinaria`. Se conectan por HTTP: el frontend llama a `http://localhost:8080/api/v1/...` y manda el token en la cabecera `Authorization: Bearer <token>`.

## Requisitos

- Java JDK 17 o superior.
- No hace falta instalar Maven: el proyecto trae `mvnw`.
- Para la versión con PostgreSQL: Docker Desktop.

## Cómo ejecutarlo (sin Docker, con H2)

En Windows:

```
mvnw.cmd spring-boot:run
```

En Mac o Linux:

```
./mvnw spring-boot:run
```

El servidor queda en `http://localhost:8080`.

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:veterinaria`, usuario `sa`, sin contraseña).

## Usuarios de prueba

Se crean solos al iniciar (`config/DatosIniciales.java`). La contraseña de todos es `SanMarcos2026!`.

| Correo | Rol |
| --- | --- |
| brun.rivera@duocuc.cl | Administrador |
| germ.pino@duocuc.cl | Administrador |
| recepcion@sanmarcos.cl | Recepcionista |
| cliente@sanmarcos.cl | Dueño de mascota |

## Endpoints

| Método | Ruta | Quién puede | Qué hace |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/login` | Público | Recibe `{email, password}` y devuelve `{token, usuario}` |
| POST | `/api/v1/auth/renovar` | Con token | Cambia un token válido por uno nuevo |
| GET | `/api/v1/citas` | Administrador, Recepcionista | Lista todas las citas |
| GET | `/api/v1/citas/{id}` | Administrador, Recepcionista | Busca una cita |
| POST | `/api/v1/citas` | Administrador, Recepcionista | Crea una cita |
| PUT | `/api/v1/citas/{id}` | Administrador, Recepcionista | Actualiza una cita |
| DELETE | `/api/v1/citas/{id}` | Administrador | Elimina una cita |
| GET | `/api/v1/usuarios` | Administrador | Lista los usuarios |
| POST | `/api/v1/usuarios` | Administrador | Crea un usuario |
| PUT | `/api/v1/usuarios/{id}` | Administrador | Edita, activa o desactiva un usuario |

Respuestas de error, siempre con el formato `{ "mensaje": "..." }`:

- `400`: faltan datos, correo repetido, choque de horario, etc.
- `401`: no hay token, el token es falso o ya venció.
- `403`: el token es válido pero el rol no tiene permiso.
- `404`: la cita o el usuario no existe.

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

### Probar en Swagger

1. Abre `POST /api/v1/auth/login`, "Try it out", y manda `{"email":"brun.rivera@duocuc.cl","password":"SanMarcos2026!"}`.
2. Copia el `token` de la respuesta.
3. Arriba a la derecha, botón **Authorize**, pega el token y acepta.
4. Ahora los endpoints de citas y usuarios responden.

## Cómo funciona el JWT

1. El usuario hace login. `UsuarioService` busca el correo y compara la contraseña con **BCrypt** (las contraseñas se guardan encriptadas).
2. Si está bien, `JwtUtil` crea un token firmado con HS256 que tiene: correo (`sub`), rol, nombre, fecha de creación y fecha de vencimiento (1 hora).
3. El frontend guarda el token y lo manda en cada petición: `Authorization: Bearer <token>`.
4. `JwtFiltro` revisa la firma y el vencimiento en cada petición. Si está bien, busca al usuario en la BD (por si lo desactivaron o le cambiaron el rol) y le dice a Spring Security qué rol tiene.
5. `SecurityConfig` decide si ese rol puede usar la ruta. Si no hay token: 401. Si el rol no alcanza: 403.
6. Antes de que venza, el frontend llama a `/api/v1/auth/renovar` y recibe un token nuevo.

La clave para firmar está en `application.properties` (`jwt.secreto`) y se puede cambiar con la variable de entorno `JWT_SECRETO`.

## Estructura

- `model/`: entidades JPA `Cita` (tabla `citas`) y `Usuario` (tabla `usuarios`).
- `repository/`: acceso a la base de datos con `JpaRepository`.
- `service/CitaService.java`: datos obligatorios y que un veterinario no tenga dos citas a la misma hora.
- `service/UsuarioService.java`: login, correos únicos, contraseñas con BCrypt y que siempre quede un administrador activo.
- `controller/`: endpoints REST (`AuthController`, `CitaController`, `UsuarioController`), documentados con anotaciones de Swagger.
- `security/`: `JwtUtil` (crear y leer tokens) y `JwtFiltro` (revisa el token en cada petición).
- `config/SecurityConfig.java`: qué rol puede usar cada ruta.
- `config/CorsConfig.java`: desde qué páginas se puede llamar a la API.
- `config/SwaggerConfig.java`: título de la documentación y botón "Authorize".
- `config/DatosIniciales.java`: crea los usuarios de prueba.
- `exception/`: excepciones y manejo de errores con `@RestControllerAdvice`.

## Pruebas

```
mvnw.cmd test
```

`SeguridadTests` levanta la aplicación y prueba: 401 sin token, 401 con clave incorrecta o token falso, 403 cuando el rol no alcanza, crear una cita, el choque de horario y la renovación del token.

## Base de datos

Sin Docker se usa H2 en memoria. Al iniciar se cargan citas de ejemplo desde `data.sql`. Los datos se borran al detener el servidor.

## Ejecutar con Docker (PostgreSQL)

```
docker compose up --build
```

Esto levanta dos contenedores: `db` (PostgreSQL 16) y `backend` (esta API). La API queda en `http://localhost:8080`. Dentro de Docker el backend se conecta a la base con el nombre del servicio (`db`), no con `localhost`.

- El backend espera a que PostgreSQL esté listo (`healthcheck` + `depends_on`).
- Los datos se guardan en el volumen `db_data`, así que no se pierden al reiniciar.
- Se usa el perfil `docker` (`application-docker.properties`) y `data-postgres.sql`.
- El puerto 5432 de la base de datos no se publica: solo el backend la usa, por la red interna.
- Para apagar: `docker compose down` (agrega `-v` para borrar también los datos).
- Claves y orígenes se pueden cambiar con un archivo `.env` (ver `.env.example`).

Comandos útiles:

```
docker compose ps
docker compose logs -f backend
docker exec -it veterinaria-db psql -U vet -d veterinaria
```

## Desplegar en AWS EC2

Ver [AWS.md](AWS.md).
