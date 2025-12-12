FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN ./mvnw dependency:resolve -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /builder
COPY --from=builder /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

COPY --from=extractor /builder/dependencies/ ./
COPY --from=extractor /builder/spring-boot-loader/ ./
COPY --from=extractor /builder/snapshot-dependencies/ ./
COPY --from=extractor /builder/application/ ./


ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]