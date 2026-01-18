FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/notification-api-*-SNAPSHOT.jar /app/notification-api.jar

RUN chmod -R +x /app

CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/notification-api.jar"]