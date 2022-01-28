#!/bin/bash
echo ecs
echo "ecs address port is $ECS:$ecs kv-server port is $1 and log level is $2"
#if [ -z ${+x} ];
#  then echo "ECS ip:port should be set"; exit;
#fi
if [ -z ${1+x} ];
  then echo "KV Server port should be set"; exit;
fi
if [ -z ${2+x} ];
  then java -jar target/kv-server.jar -b ecs -p $1;
  else java -jar target/kv-server.jar -b "$ECS_PORT_55430_TCP_ADDR:$ECS_PORT_55430_TCP_PORT" -p $1 -ll $2;
fi