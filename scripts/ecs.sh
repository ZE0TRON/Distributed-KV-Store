#!/bin/bash
echo "log level is $1"
if [ -z ${1+x} ];
  then java -jar target/ecs-server.jar -a 0.0.0.0 -p 55430;
  else java -jar target/ecs-server.jar -a 0.0.0.0 -p 55430 -ll $1;
fi