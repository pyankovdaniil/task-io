FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/authentication.jar
COPY ./target/authentication.jar authentication.jar
ENTRYPOINT ["java", "-jar", "/authentication.jar"]