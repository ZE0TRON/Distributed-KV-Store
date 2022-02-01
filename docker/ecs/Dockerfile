FROM maven:latest
WORKDIR /project
ADD ../../ .
RUN mvn -Dmaven.test.skip=true install

ENTRYPOINT ["./docker/ecs/ecs.sh"]
