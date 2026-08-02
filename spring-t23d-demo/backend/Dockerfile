FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests

FROM eclipse-temurin:21-jre
# docker-cli wird benoetigt, damit das Backend per ProcessBuilder ("docker run ...")
# Sibling-Container auf dem Host starten kann (Docker-Socket wird gemountet, siehe docker-compose.yml).
RUN apt-get update && apt-get install -y docker.io --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
