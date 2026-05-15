FROM eclipse-temurin:24-alpine
COPY target/*.jar user.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "user.jar"]