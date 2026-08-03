# Compila el backend con Java 21.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copia primero Maven para aprovechar la cache.
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Descarga las dependencias.
RUN ./mvnw dependency:go-offline -B

# Copia y compila el codigo fuente.
COPY src src

RUN ./mvnw clean package -DskipTests -B \
    && cp target/medical-api-0.0.1-SNAPSHOT.jar /app/app.jar

# Imagen final ligera con Java Runtime.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crea un usuario sin privilegios.
RUN addgroup -S zenticode \
    && adduser -S zenticode -G zenticode

# Copia solamente el JAR compilado.
COPY --from=build --chown=zenticode:zenticode /app/app.jar /app/app.jar

USER zenticode

EXPOSE 10000

# Ejecuta Spring Boot respetando la memoria disponible.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-XX:InitialRAMPercentage=20.0", "-XX:+UseSerialGC", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]