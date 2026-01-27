# ChurnInsight API - Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=for-the-badge&logo=spring)
![Maven](https://img.shields.io/badge/Maven-3.9+-red?style=for-the-badge&logo=apache-maven)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**API REST para predicción de churn de clientes utilizando Machine Learning**

</div>

---

ChurnInsight es una API REST desarrollada con Spring Boot que predice si un cliente es propenso a cancelar un servicio (churn). Este proyecto fue desarrollado como parte del desafío para la Hackathon ONE.

## 📋 Descripción

ChurnInsight proporciona un servicio completo de predicción de churn que analiza diferentes características del cliente (edad, salario, balance, productos, género, estado de membresía, etc.) para determinar la probabilidad de que un cliente cancele su servicio. La API incluye:

- **Predicción individual** con y sin explicabilidad (top 3 features más influyentes)
- **Predicción batch** mediante archivos CSV con procesamiento asíncrono
- **Estadísticas y métricas** de predicciones históricas
- **Persistencia de datos** en PostgreSQL (producción) o H2 (desarrollo)
- **Documentación interactiva** con Swagger/OpenAPI

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Propósito |
|------------|-----------|
| Java 17+ | Lenguaje base |
| Spring Boot 3.5.9 | Framework |
| Maven 3.6+ | Gestión de dependencias |
| PostgreSQL | Base de datos de producción |
| H2 Database | Base de datos en memoria para desarrollo |


### **Dependencias**:
- **Spring Web MVC** - API REST
- **Spring Data JPA** - Persistencia de datos
- **Spring Validation** - Validación de entrada
- **MapStruct** - Mapeo de DTOs
- **Lombok** - Reducción de código boilerplate
- **SpringDoc OpenAPI (Swagger) 2.8.8** - Documentación de la API 
- **WebFlux/WebClient** - Cliente HTTP para servicio de ML
- **Apache Commons CSV** - Procesamiento de archivos CSV
- **JaCoCo** - Cobertura de código (mínimo 80%)

----

## 📦 Requisitos Previos

- Java 17 o superior
- Maven 3.6+ (o usar el wrapper incluido `mvnw`)
- Un servicio de modelo Python ejecutándose (por defecto en `http://localhost:8000`)
- PostgreSQL (para producción) o H2 (para desarrollo)


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

---


## ⚙️ Configuración

### Variables de Entorno

La aplicación utiliza variables de entorno para la configuración:

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev/prod) | `prod` |
| `SERVER_PORT` | Puerto del servidor | `8080` |
| `MODEL_SERVICE_URL` | URL del servicio de modelo Python | `http://localhost:8000` |
| `DB_URL` | URL de PostgreSQL (prod) | `jdbc:postgresql://localhost:5432/churninsight` |
| `DB_USERNAME` | Usuario de base de datos (prod) | `postgres` |
| `DB_PASSWORD` | Contraseña de base de datos (prod) | - |

### Perfiles

La aplicación soporta múltiples perfiles de configuración:

- **dev**: Perfil de desarrollo 
- **test**: Perfil de pruebas
- **prod**: Perfil de producción

Los archivos de configuración se encuentran en `src/main/resources/` y `src/test/resources/`:
- `application.yml` - Configuración base
- `application-dev.yml` - Configuración de desarrollo
- `application-test.yml` - Configuración de pruebas
- `application-prod.yml` - Configuración de producción

---


## 📚 Documentación de la API

La API incluye documentación OpenAPI (Springdoc) con ejemplos anotados en los endpoints.

- **Swagger UI (interactivo con ejemplos):** `http://localhost:8080/swagger-ui.html` (redirige a la UI actual y muestra los ejemplos de petición y respuesta).
- **OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`

> Notas: los endpoints en `PredictionController` están anotados con `@Operation` y proveen ejemplos visibles en Swagger UI para facilitar pruebas rápidas.

---


## 🏗️ Arquitectura de la solución

La solución implementa una arquitectura por capas siguiendo principios SOLID y buenas prácticas de Spring Boot:

### Capas Principales

**1. Capa de Presentación (Controllers)**
- Exposición de endpoints REST
- Validación de entrada con Bean Validation
- Manejo de respuestas HTTP y códigos de estado
- Documentación con anotaciones OpenAPI

**2. Capa de Aplicación (Services)**
- Orquestación de casos de uso
- Lógica de negocio
- Coordinación entre controladores, repositorios y servicios externos
- Procesamiento asíncrono de batches

**3. Capa de Dominio (Entities)**
- Modelos de negocio (Customer, Prediction, FeatureImportance, BatchPredictionJob)
- Reglas de negocio independientes de frameworks
- Relaciones JPA entre entidades

**4. Capa de Persistencia (Repositories)**
- Acceso a datos con Spring Data JPA
- Consultas personalizadas con JPQL
- Índices optimizados para consultas frecuentes

**5. Capa de Infraestructura**
- Cliente HTTP para servicio de ML externo
- Mappers para conversión de DTOs
- Configuración de beans y componentes

Este enfoque permite:
- Cambiar el modelo predictivo sin afectar la API
- Testear cada capa de forma aislada
- Escalar el proyecto fácilmente
- Mantener el código limpio y organizado

---

## 🏗️ Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/one/hackathonlatam/dic25equipo69/churninsight/
│   │   │   ├── ChurninsightApplication.java      # Clase principal
│   │   │   ├── client/
│   │   │   │   └── ModelClientService.java       # Cliente HTTP para modelo ML
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java            # Configuración Swagger
│   │   │   │   ├── WebClientConfig.java          # Configuración WebClient
│   │   │   │   └── AsyncConfig.java              # Configuración procesamiento asíncrono
│   │   │   ├── controller/
│   │   │   │   ├── PredictionController.java     # Predicciones individuales
│   │   │   │   ├── BatchPredictionController.java # Predicciones batch
│   │   │   │   └── StatsController.java          # Estadísticas y métricas
│   │   │   ├── dto/
│   │   │   │   ├── enums/                        # Enumeraciones (Gender, Geography, etc.)
│   │   │   │   ├── request/                      # DTOs de solicitud
│   │   │   │   └── response/                     # DTOs de respuesta
│   │   │   ├── entity/
│   │   │   │   ├── Customer.java                 # Entidad de cliente
│   │   │   │   ├── Prediction.java               # Entidad de predicción
│   │   │   │   ├── FeatureImportance.java        # Importancia de features
│   │   │   │   ├── BatchPredictionJob.java       # Job de batch
│   │   │   │   └── BatchPredictionResult.java    # Resultado de batch
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java   # Manejo global de excepciones
│   │   │   ├── mapper/
│   │   │   │   ├── CustomerMapper.java           # Mapeo de Customer
│   │   │   │   └── PredictionMapper.java         # Mapeo de Prediction
│   │   │   ├── repository/
│   │   │   │   ├── CustomerRepository.java       # Repositorio de clientes
│   │   │   │   ├── PredictionRepository.java     # Repositorio de predicciones
│   │   │   │   ├── FeatureImportanceRepository.java
│   │   │   │   ├── BatchPredictionJobRepository.java
│   │   │   │   └── BatchPredictionResultRepository.java
│   │   │   └── service/
│   │   │       ├── IPredictionService.java       # Interfaz de predicción
│   │   │       ├── IBatchPredictionService.java  # Interfaz de batch
│   │   │       ├── IStatsService.java            # Interfaz de estadísticas
│   │   │       ├── FeatureExplainerService.java  # Servicio de explicabilidad
│   │   │       └── impl/                         # Implementaciones
│   │   └── resources/
│   │       ├── application.yml                    # Configuración base
│   │       ├── application-dev.yml                # Configuración desarrollo
│   │       └── application-prod.yml               # Configuración producción
│   └── test/                                      # Tests unitarios e integración
├── pom.xml                                        # Configuración Maven
├── mvnw                                           # Maven Wrapper (Linux/Mac)
└── mvnw.cmd                                       # Maven Wrapper (Windows)
```

---

## 📡 Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### 1. Predicción Individual

Solicitud para realizar una predicción de churn. Si la ausencia de datos impide la predicción o la validación falla, la API devolverá un error (400) con un mensaje explicativo.

#### POST `/predict`
Predicción básica sin explicabilidad.

**Request:**
```json
{
  "customerId": "CUST-12345",
  "geography": "Spain",
  "gender": "Male",
  "age": 42,
  "creditScore": 650,
  "balance": 1000.50,
  "estimatedSalary": 50000.00,
  "tenure": 5,
  "numOfProducts": 2,
  "satisfactionScore": 3,
  "isActiveMember": true,
  "hasCrCard": true,
  "complain": false
}
```

- `customerId` (string): ID del cliente de la base de datos donde proviene (opcional).
- `geography` (string) — País o región del cliente. (spain, france, germany)
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

**Response:**
```json
{
  "clientId": "CUST-12345",
  "forecast": "Va a cancelar",
  "probability": 0.81,
  "timestamp": "2026-01-27T10:00:00"
}
```

Campos de respuesta:
- `forecast` (string) — Etiqueta o mensaje de la predicción (ej.: `Va a cancelar`, `No va a cancelar`).
- `probability` (float) — Valor entre `0.0` y `1.0` que indica la probabilidad de la predicción.

#### POST `/predict/with-explanation`
Predicción con explicabilidad (top 3 features más influyentes).

**Response:**
```json
{
  "clientId": "a1b2-c3d4-e5f6",
  "forecast": "Va a cancelar",
  "probability": 0.81,
  "timestamp": "2026-01-27T10:00:00",
  "topFeatures": [
    {
      "name": "age",
      "displayName": "Edad",
      "value": "42",
      "impact": "NEGATIVE"
    },
    {
      "name": "numOfProducts",
      "displayName": "Número de Productos",
      "value": "2",
      "impact": "POSITIVE"
    },
    {
      "name": "isActiveMember",
      "displayName": "Miembro Activo",
      "value": "true",
      "impact": "POSITIVE"
    }
  ],
  "riskLevel": "HIGH"
}
```

Campos de respuesta:
- `forecast` (string) — Etiqueta o mensaje de la predicción (ej.: `Va a cancelar`, `No va a cancelar`).
- `probability` (float) — Valor entre `0.0` y `1.0` que indica la probabilidad de la predicción.
- `top_features` (array) — Top 3 factores mas importantes para la predicción.
- `name` (string) — Nombre del campo (del factor importante).
- `value` (float) — Valor del campo (del factor importante).
- `impact` (string) — Valor "positivo" en dirección para churn o "negativo" en no churn.
- `riskLevel` (string) — Nivel de riesgo (bajo, medio, alto) de la predicción.

#### GET `/predict/history`
Obtiene las últimas 10 predicciones.

#### GET `/predict/stats`
Obtiene estadísticas básicas de predicciones.

### 2. Predicción Batch

#### POST `/predict/batch`
Inicia un proceso batch desde archivo CSV.

**Request:** `multipart/form-data` con archivo CSV

**Response:**
```json
{
  "batchId": "batch-uuid-123",
  "status": "PROCESSING",
  "message": "Batch iniciado correctamente"
}
```

#### GET `/predict/batch/{batchId}`
Consulta el estado de un batch.

**Response:**
```json
{
  "batchId": "batch-uuid-123",
  "status": "COMPLETED",
  "totalRecords": 100,
  "processedRecords": 100,
  "successfulPredictions": 95,
  "failedPredictions": 5,
  "startTime": "2026-01-27T10:00:00",
  "endTime": "2026-01-27T10:05:00"
}
```

#### GET `/predict/batch/{batchId}/results`
Obtiene los resultados de un batch con paginación.

**Query Params:**
- `page` (default: 0)
- `size` (default: 10)
- `successOnly` (default: false)
- `errorsOnly` (default: false)

### 3. Estadísticas

#### GET `/stats`
Obtiene estadísticas generales.

**Query Params:**
- `period` (opcional): Período en días (7, 30, 90)

**Response:**
```json
{
  "totalPredictions": 1000,
  "churnPredictions": 350,
  "noChurnPredictions": 650,
  "churnRate": 0.35,
  "averageProbability": 0.45,
  "period": 30
}
```

#### GET `/stats/high-risk`
Obtiene clientes de alto riesgo (probabilidad >= 0.7).

**Query Params:**
- `period` (default: 30)
- `page` (default: 0)
- `size` (default: 10)
---

## 🔧 Desarrollo

### Ejecutar en modo desarrollo

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Ejecutar tests

```bash
./mvnw test
```

### Ejecutar tests con cobertura

```bash
./mvnw verify
```

El reporte de cobertura se genera en `target/site/jacoco/index.html`

### Compilar sin ejecutar tests

```bash
./mvnw clean install -DskipTests
```
---

## 📝 Modelo de Datos

### Entidades Principales

#### Customer
- `id`: ID técnico (autoincremental)
- `customerId`: ID de negocio del cliente
- `geography`: País/región (SPAIN, FRANCE, GERMANY)
- `gender`: Género (MALE, FEMALE)
- `age`: Edad
- `creditScore`: Puntaje de crédito
- `balance`: Balance de cuenta
- `estimatedSalary`: Salario estimado
- `tenure`: Antigüedad (meses)
- `numOfProducts`: Número de productos
- `satisfactionScore`: Puntuación de satisfacción (1-5)
- `isActiveMember`: Si es miembro activo
- `hasCrCard`: Si tiene tarjeta de crédito
- `complain`: Si ha presentado quejas


Los campos `gender`, `estimatedSalary`, `tenure` y `hasCrCard` son **opcionales**.

Las siguientes reglas aseguran que los datos de entrada cumplan con rangos y valores esperados:

| Variable            | Dominio esperado                       |
|---------------------|-----------------------------------------|
| geography            | Spain, France, Germany                  |
| gender               | Male, Female                           |
| age                  | 18 – 100                                |
| creditScore          | 100 – 1000                              |
| balance              | ≥ 0                                    |
| estimatedSalary      | ≥ 0                                    |
| tenure               | 0 – 20                                 |
| numOfProducts        | 1, 2, 3, 4                             |
| satisfactionScore    | 1, 2, 3, 4, 5                           |
| variables binarias   | true / false                           |

#### Prediction
- `id`: ID de la predicción
- `customer`: Relación con Customer (FK)
- `predictionResult`: Resultado booleano (true = churn)
- `probability`: Probabilidad (0.0 - 1.0)
- `createdAt`: Timestamp de creación
- `featureImportances`: Lista de features más influyentes

#### FeatureImportance
- `id`: ID de la feature
- `prediction`: Relación con Prediction (FK)
- `name`: Nombre técnico de la feature
- `displayName`: Nombre para mostrar
- `featureValue`: Valor de la feature
- `impactDirection`: Dirección del impacto (POSITIVE, NEGATIVE)
- `rankPosition`: Posición en el ranking (1-3)

#### BatchPredictionJob
- `id`: ID del batch
- `status`: Estado (PENDING, PROCESSING, COMPLETED, FAILED, PARTIAL)
- `filename`: Nombre del archivo CSV
- `totalRecords`: Total de registros
- `processedRecords`: Registros procesados
- `successfulPredictions`: Predicciones exitosas
- `failedPredictions`: Predicciones fallidas
- `startTime`: Hora de inicio
- `endTime`: Hora de finalización
- `errorMessage`: Mensaje de error (si aplica)

## 🔌 Integración con Modelo Python

La aplicación se conecta a un servicio de modelo Python externo. Asegúrate de que el servicio esté ejecutándose y accesible en la URL configurada en `MODEL_SERVICE_URL`.

* **Endpoint**: `/predict` (POST)
* **URL por defecto**: `http://localhost:8000`

Asegúrate de que el servicio esté ejecutándose y accesible.

---

## 🐛 Manejo de Errores

La aplicación incluye un manejador global de excepciones (`GlobalExceptionHandler`) que proporciona respuestas de error consistentes en formato JSON.


### Códigos de Error HTTP

* `400 Bad Request`: Validación fallida o datos inválidos
* `404 Not Found`: Recurso no encontrado
* `500 Internal Server Error`: Error interno del servidor
* `503 Service Unavailable`: Servicio de modelo no disponible

### Formato de Error

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El campo 'age' es obligatorio",
  "timestamp": "2026-01-25T14:30:22"
}
```

Notas:
- Respuestas de error (400/422/500) siguen el formato definido por `GlobalExceptionHandler` (ej.: `timestamp`, `status`, `error`, `message`).
- Asegúrate de enviar todos los campos con el tipo correcto para evitar errores de validación.

---


## 🚀 Despliegue

### Docker

El proyecto incluye un Dockerfile para despliegue en contenedores:

```bash
# Construir imagen
docker build -t backend .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e MODEL_SERVICE_URL=http://model-service:8000 \
  backend
```

### Variables de Entorno en Producción

```bash
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8080
export MODEL_SERVICE_URL=https://model-service.example.com
```

---

## 👥 Equipo

Desarrollado por el Equipo 69 para la Hackathon ONE.

<table>
<tr>
<td align="center" width="150">
<sub><b>Anghelo Flores</b></sub><br />

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/anghelo-flores-4725451b1/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/evanghel1on)
</td>
<td align="center" width="150">
<sub><b>Andrea Cecilia Lopez</b></sub><br />


[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/andreacecilialopez)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ProfeCeci)
</td>
<td align="center" width="150">
<sub><b>Ashley Villanueva</b></sub><br />

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://pe.linkedin.com/in/ashley-zifrikc-dev)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Zifrikc)
</td>
<td align="center" width="150">
<sub><b>Luis Fernando Jaramillo</b></sub><br />

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/jaramilloster)
</td>
<td align="center" width="150">
<sub><b>Enrique Castillo</b></sub><br />

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/joseenriquecastillo/)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/kikecastillocolombia)
</td>
</tr>
</table>

---


## 📄 Licencia

Este proyecto es desarrollado para la Hackathon ONE - Diciembre 2025, Equipo 69.


---

<div align="center">

**Desarrollado con ❤️ por el Equipo ChurnInsight**


</div>

