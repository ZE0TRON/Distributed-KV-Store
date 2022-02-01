#KV-Persisted DB Server and Client


## Docker Usage
### Start the cluster
```bash
cd docker
docker compose build
docker compose up -d --scale kvs=<number_of_servers> --scale client=<number_of_clients>
```
### Connect to the client
```bash
docker ps (get the client container id to connect)
docker attach <client_container_id>
activate (needed for client to start reading from console while running in a container)
```
