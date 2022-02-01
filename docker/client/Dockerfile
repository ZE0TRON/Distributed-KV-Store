FROM maven:latest
WORKDIR /client
ADD ../../ .
RUN mvn -Dmaven.test.skip=true install

ENTRYPOINT ["./docker/client/client.sh"]
