FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN ./mvnw dependency:resolve -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV SERVER_PORT=$PORT

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]