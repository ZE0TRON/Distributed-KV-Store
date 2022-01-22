#KV-Persisted DB Server and Client


## Docker Usage
Current docker configuration implemented in a way that ECS is always started with port.

### ECS
ECS can be build for docker using command from the project root

`` docker build -t ecs -f ./docker/ecs/Dockerfile .``

ECS can be run using docker command

``docker run --expose 55430 --name ecs -t ecs ``

### KV Server
KV Server can be build for docker using command from the project root

`` docker build -t kvs -f ./docker/kvs/Dockerfile .``

KV Server can be run using docker command

``docker run --expose <kv_server_port> --link ecs kvs <kv_server_port>``