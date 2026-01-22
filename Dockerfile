# Etapa 1: Construcción (Usamos versión estándar, no Alpine)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Empaquetar
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Usamos versión estándar compatible con ARM64)
FROM eclipse-temurin:17-jdk
WORKDIR /app
# Copiar el .jar generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
