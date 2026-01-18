FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/notification-auth-*-SNAPSHOT.jar /app/notification-auth.jar

RUN chmod -R +x /app

CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/notification-auth.jar"]