FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/notification-exec.jar
COPY ./target/notification-exec.jar notification.jar
ENTRYPOINT ["java", "-jar", "/notification.jar"]