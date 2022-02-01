#!/bin/bash
if [ -z ${1+x} ];
  then java -jar target/kv-server.jar -bh ecs -bp 55430;
  else java -jar target/kv-server.jar -bh ecs -bp 55430 -p $1;
fi