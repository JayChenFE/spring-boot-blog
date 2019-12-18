FROM openjdk:11-alpine
RUN mkdir /app
WORKDIR /app
COPY target/gs-spring-boot-0.1.0.jar /app
EXPOSE 8080
CMD [ "java", "-jar", "gs-spring-boot-0.1.0.jar" ]