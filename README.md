# MiniMarket Plus API

API REST en Spring Boot para la gestión de un minimarket: productos, categorías, carrito, inventario, ventas y seguridad con JWT, LDAP embebido y soporte opcional OAuth2.

## Requisitos

- **Java 17** o superior (`java -version`)
- Conexión a internet en la **primera ejecución** (Maven Wrapper descarga dependencias automáticamente)
- No se requiere Maven instalado ni base de datos externa (usa H2 en memoria)

## Ejecución

### Windows

.\mvnw.cmd spring-boot:run

La aplicación inicia en **http://localhost:8081** (puerto configurado en `application.properties`, no el 8080 por defecto por que lo tenia ocupado en el dispositivo donde se desarrolló).

## Ejecutar pruebas

.\mvnw.cmd test


Para generar reporte de cobertura JaCoCo:

.\mvnw.cmd verify

El reporte queda en `target/site/jacoco/index.html`.

## Estructura del proyecto

src/main/java/com/minimarket/
├── controller/     # Endpoints REST
├── entity/         # Entidades JPA (Carrito, Inventario, Producto, etc.)
├── repository/     # Acceso a datos
├── service/        # Lógica de negocio
└── security/       # JWT, LDAP, OAuth2, auditoría

src/test/java/      # Pruebas unitarias e integración (JUnit 5 + Mockito)