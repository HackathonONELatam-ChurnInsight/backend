# ChurnInsight API

ChurnInsight es una API REST desarrollada con Spring Boot que predice si un cliente es propenso a cancelar un servicio (churn). Este proyecto fue desarrollado como parte del desafío para la Hackathon ONE.

## 📋 Descripción

ChurnInsight proporciona un servicio de predicción de churn que analiza diferentes características del cliente (edad, salario, balance, productos, género, estado de membresía, etc.) para determinar la probabilidad de que un cliente cancele su servicio.

## 🛠️ Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Web MVC**
- **Spring Validation**
- **Lombok**
- **SpringDoc OpenAPI (Swagger)** - Para documentación de la API
- **Maven** - Gestión de dependencias

## 📦 Requisitos Previos

- Java 17 o superior
- Maven 3.6+ (o usar el wrapper incluido `mvnw`)
- Un servicio de modelo Python ejecutándose (por defecto en `http://localhost:8000`)

## 🚀 Instalación

1. Clona el repositorio:
```bash
git clone <url-del-repositorio>
cd backend
```

2. Compila el proyecto:
```bash
./mvnw clean install
```

3. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```

O si prefieres usar Java directamente:
```bash
java -jar target/churninsight-0.0.1-SNAPSHOT.jar
```

## ⚙️ Configuración

### Variables de Entorno

La aplicación utiliza variables de entorno para la configuración. Puedes configurarlas de las siguientes maneras:

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev/prod) | `dev` |
| `SERVER_PORT` | Puerto del servidor | `8080` |
| `MODEL_SERVICE_URL` | URL del servicio de modelo Python | `http://localhost:8000` |

### Perfiles

La aplicación soporta múltiples perfiles de configuración:

- **dev**: Perfil de desarrollo (por defecto)
- **prod**: Perfil de producción

Los archivos de configuración se encuentran en `src/main/resources/`:
- `application.yml` - Configuración base
- `application-dev.yml` - Configuración de desarrollo
- `application-prod.yml` - Configuración de producción

## 📚 Documentación de la API

Una vez que la aplicación esté ejecutándose, puedes acceder a la documentación interactiva de la API (Swagger UI) en:

```
http://localhost:8080/swagger-ui.html
```

O la especificación OpenAPI en formato JSON:
```
http://localhost:8080/v3/api-docs
```

## 🏗️ Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/one/hackathonlatam/dic25equipo69/churninsight/
│   │   │   ├── ChurninsightApplication.java      # Clase principal
│   │   │   ├── client/
│   │   │   │   └── ModelClientService.java       # Cliente para servicio de modelo
│   │   │   ├── config/
│   │   │   │   └── OpenApiConfig.java            # Configuración OpenAPI
│   │   │   ├── controller/
│   │   │   │   └── PredictionController.java    # Controlador REST
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── PredictionRequestDTO.java # DTO de solicitud
│   │   │   │   │   └── Gender.java               # Enum de género
│   │   │   │   └── response/
│   │   │   │       ├── PredictionResponseDTO.java # DTO de respuesta
│   │   │   │       └── ErrorResponseDTO.java      # DTO de error
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java   # Manejador global de excepciones
│   │   │   └── service/
│   │   │       ├── IPredictionService.java       # Interfaz del servicio
│   │   │       └── impl/
│   │   │           └── PredictionService.java    # Implementación del servicio
│   │   └── resources/
│   │       ├── application.yml                    # Configuración base
│   │       ├── application-dev.yml                # Configuración desarrollo
│   │       └── application-prod.yml                # Configuración producción
│   └── test/                                      # Tests unitarios
├── pom.xml                                        # Configuración Maven
├── mvnw                                           # Maven Wrapper (Linux/Mac)
└── mvnw.cmd                                       # Maven Wrapper (Windows)
```

## 📡 Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints Disponibles

Los endpoints están definidos en `PredictionController` y siguen el patrón REST.

## 🔧 Desarrollo

### Ejecutar en modo desarrollo

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Ejecutar tests

```bash
./mvnw test
```

### Compilar sin ejecutar tests

```bash
./mvnw clean install -DskipTests
```

## 📝 Modelo de Datos

### PredictionRequestDTO

Solicitud para realizar una predicción de churn. **Los campos son opcionales**: puedes enviar los que tengas disponibles y el modelo intentará predecir con la información recibida. Si la ausencia de datos impide la predicción o la validación falla, la API devolverá un error (400) con un mensaje explicativo.

Ejemplo (JSON):

```json
{
  "geography": "Spain",
  "gender": "Male",
  "age": 42,
  "creditScore": 650,
  "balance": 14.5,
  "estimatedSalary": 14.0,
  "tenure": 6,
  "numOfProducts": 5,
  "satisfactionScore": 2,
  "isActiveMember": true,
  "hasCrCard": true,
  "complain": false
}
```

Campos:
- `geography` (string) — País o región del cliente.
- `gender` (string) — Género (ej.: `Male`, `Female`) según el dataset.
- `age` (int) — Edad del cliente (entero positivo).
- `creditScore` (int) — Puntaje de crédito (entero).
- `balance` (float) — Balance de la cuenta.
- `estimatedSalary` (float) — Salario estimado.
- `tenure` (int) — Tiempo con la compañía (meses/periodos).
- `numOfProducts` (int) — Número de productos contratados.
- `satisfactionScore` (int) — Puntuación de satisfacción (ej.: escala 1-5).
- `isActiveMember` (bool) — Si es miembro activo.
- `hasCrCard` (bool) — Si posee tarjeta de crédito.
- `complain` (bool) — Si ha presentado quejas.

### PredictionResponseDTO

Respuesta con la predicción.

Ejemplo (200 OK):

```json
{
  "forecast": "Va a cancelar",
  "probability": 0.81
}
```

Campos de respuesta:
- `forecast` (string) — Etiqueta o mensaje de la predicción (ej.: `Va a cancelar`, `No cancelará`).
- `probability` (float) — Valor entre `0.0` y `1.0` que indica la probabilidad de la predicción.

Notas:
- Respuestas de error (400/422/500) siguen el formato definido por `GlobalExceptionHandler` (ej.: `timestamp`, `status`, `error`, `message`, `path`).
- Asegúrate de enviar todos los campos con el tipo correcto para evitar errores de validación.

## 🔌 Integración con Modelo Python

La aplicación se conecta a un servicio de modelo Python externo. Asegúrate de que el servicio esté ejecutándose y accesible en la URL configurada en `MODEL_SERVICE_URL`.

## 🐛 Manejo de Errores

La aplicación incluye un manejador global de excepciones (`GlobalExceptionHandler`) que proporciona respuestas de error consistentes en formato JSON.

## 📄 Licencia

Este proyecto es desarrollado para la Hackathon ONE - Diciembre 2025, Equipo 69.

## 👥 Equipo

Desarrollado por el Equipo 69 para la Hackathon ONE.

---

**Nota**: Este proyecto está en desarrollo activo. Algunas funcionalidades pueden estar en proceso de implementación.
