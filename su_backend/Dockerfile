FROM docker.io/library/maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN ./mvnw dependency:resolve -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

# Uruchamiamy skrypt
ENTRYPOINT ["./entrypoint.sh"]