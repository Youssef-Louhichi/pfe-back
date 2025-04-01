FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/pfe-back-0.0.1-SNAPSHOT.jar pfe-back.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "pfe-back.jar"]
