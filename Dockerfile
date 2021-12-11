FROM adoptopenjdk/openjdk11:latest

VOLUME /temp

ADD target/KVClient.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]