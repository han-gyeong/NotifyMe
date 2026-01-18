FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/notification-scheduler-*-SNAPSHOT.jar /app/notification-scheduler.jar

RUN chmod -R +x /app

CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/notification-scheduler.jar"]