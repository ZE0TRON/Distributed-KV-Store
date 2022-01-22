FROM maven:latest
WORKDIR /ecs
ADD ../../ .
RUN mvn -Dmaven.test.skip=true install

ENTRYPOINT ["./docker/ecs/ecs.sh"]
