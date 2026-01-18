FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/notification-gateway-*-SNAPSHOT.jar /app/notification-gateway.jar

RUN chmod -R +x /app

CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/notification-gateway.jar"]