FROM maven:latest
WORKDIR /kvs
ADD ../../ .
RUN mvn -Dmaven.test.skip=true install

ENTRYPOINT ["./docker/kvs/kvs.sh"]
