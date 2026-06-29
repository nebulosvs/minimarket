# MiniMarket Plus API

## Descripción

**MiniMarket Plus API** es una aplicación backend desarrollada con **Spring Boot** que implementa una API REST para la gestión de un minimarket.

El proyecto permite administrar:

* Productos
* Categorías
* Inventario
* Carrito de compras
* Ventas
* Usuarios y autenticación

Se implementaron mecanismos de seguridad mediante **Spring Security**, autenticación con **JWT**, soporte para **LDAP embebido** y configuración opcional para **OAuth2 Resource Server**. Además, se desarrollaron pruebas unitarias y de integración utilizando **JUnit 5**, **Mockito** y **JaCoCo** para validar el correcto funcionamiento del sistema.

---

# Tecnologías utilizadas

* Java 17
* Spring Boot 3
* Spring Security
* Spring Data JPA
* H2 Database
* Maven Wrapper
* JUnit 5
* Mockito
* JaCoCo
* JWT
* LDAP Embedded
* OAuth2 Resource Server (configuración disponible)

---

# Requisitos

Antes de ejecutar el proyecto asegúrate de contar con:

* Java 17 o superior
* Conexión a Internet en la primera ejecución (para descargar dependencias de Maven)

No es necesario instalar Maven ni configurar una base de datos externa, ya que el proyecto utiliza **Maven Wrapper** y una base de datos **H2 en memoria**.

---

# Ejecución del proyecto

## Windows

```bash
.\mvnw.cmd spring-boot:run
```

La aplicación quedará disponible en:

```
http://localhost:8081
```

> El puerto utilizado es **8081**, ya que durante el desarrollo el puerto 8080 se encontraba ocupado.

---

# Ejecución de pruebas

Para ejecutar todas las pruebas:

```bash
.\mvnw test
```

Para ejecutar las pruebas junto con el análisis de cobertura:

```bash
.\mvnw verify
```

El reporte HTML generado por JaCoCo se encuentra en:

```
target/site/jacoco/index.html
```

---

# Estructura del proyecto

```
src
├── main
│   ├── java
│   │   └── com.minimarket
│   │       ├── controller
│   │       ├── dto
│   │       ├── entity
│   │       ├── exception
│   │       ├── repository
│   │       ├── security
│   │       ├── service
│   │       └── config
│   └── resources
│
└── test
    └── java
        └── com.minimarket
```

---

# Seguridad implementada

El sistema incorpora distintos mecanismos de seguridad:

* Autenticación mediante JWT.
* Control de acceso basado en roles (RBAC).
* LDAP embebido para autenticación durante el desarrollo.
* Configuración para OAuth2 Resource Server.
* Protección de endpoints mediante Spring Security.
* Auditoría básica de eventos relacionados con autenticación.

Los roles utilizados son:

| Rol      | Permisos principales                       |
| -------- | ------------------------------------------ |
| GERENTE  | Administración completa del sistema        |
| EMPLEADO | Gestión de ventas e inventario             |
| CLIENTE  | Operaciones limitadas de consulta y compra |

---

# Pruebas implementadas

El proyecto incluye pruebas para validar:

* Entidades del dominio.
* Servicios.
* Controladores.
* Autenticación.
* Autorización por roles.
* Integración con Spring Security.

Las pruebas fueron desarrolladas utilizando:

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc
* JaCoCo

---

# Cobertura de pruebas

La cobertura del proyecto fue analizada mediante **JaCoCo**, verificando el cumplimiento de los principales escenarios funcionales y de seguridad definidos para la actividad.

---

# Autores

Proyecto desarrollado para la asignatura **Desarrollo Backend II (PBY2202)**.

Integrantes del equipo:

* Sebastián Tapia
* Sofía Medina
* Ángel Cáceres
