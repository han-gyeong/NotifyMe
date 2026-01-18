FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/notification-eureka-*-SNAPSHOT.jar /app/notification-eureka.jar

RUN chmod -R +x /app

CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/notification-eureka.jar"]