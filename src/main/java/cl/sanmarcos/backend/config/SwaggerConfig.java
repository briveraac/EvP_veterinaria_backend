package cl.sanmarcos.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Datos generales de la documentacion Swagger.
// @SecurityScheme agrega el boton "Authorize" en Swagger para pegar el token JWT.
// Swagger UI: http://localhost:8080/swagger-ui/index.html
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Veterinaria San Marcos",
        version = "v1",
        description = "API REST de citas y usuarios. Primero usa POST /api/v1/auth/login, copia el token y pégalo en Authorize."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SwaggerConfig {
}
