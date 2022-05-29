# Distributed Persistent Key-Value Store
A distributed persistent key-value store that uses consistent hashing and data replication.
The system consist of three main elements:
- External Configuration Service (ECS)
- Server
- Client

The external configuration service is responsible for adding and deleting new servers to the system. Whenever a server wants to join it has to communicate with the ECS to get the key range it's responsible from. Whenever a server gets stopped or crashed ECS reorganises the Hash Ring so that no data is lost and the the key range of the closed server is distributed over other servers.

## Usage
The system required minimum of 1 ECS, Server and Client.
If you want to enable data replication you should use minumum of 3 servers.

### ECS
ECS can be started using the following (check -h for all parameters):
```bash
    java -jar target/ecs-server.jar -a <address> -p <port> 
```
### Server
Server can be started using the following (check -h for all parameters):
```bash
    java -jar target/kv-server.jar -bh <ecs_address> -bp <ecs_port> 
```
### Client
Client can be started using the following (check -h for all parameters):
```bash
    java -jar target/KVClient.jar -bh <server_address> -bp <server_port>
```


## Docker Usage
### Side note about scaling
The when using high numbers of server and clients ecs sometimes face some race conditions or can get overwhelmed because of all the servers communicate with the ECS. Eventough we have a queueing mechanism at ECS it has some problems. We are planning to introduce a message queue micro-service which can solved this problem in the future releases.
### Start the cluster
```bash
docker compose build
docker compose up -d --scale kvs=<number_of_servers> --scale client=<number_of_clients>
```
### Connect to the client
```bash
docker ps (get the client container id to connect)
docker attach 
activate (needed for client to start reading from console while running in a container)
```
