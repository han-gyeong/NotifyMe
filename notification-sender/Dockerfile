FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/notification-sender-*-SNAPSHOT.jar /app/notification-sender.jar

RUN chmod -R +x /app

CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/notification-sender.jar"]