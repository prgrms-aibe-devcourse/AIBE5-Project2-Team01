FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app
COPY . .
RUN chmod +x ./mvnw && ./mvnw -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
