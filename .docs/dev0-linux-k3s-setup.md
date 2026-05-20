# Dev0 Linux k3s Setup

This document explains how to bring up the full Connect Chat stack from zero on a Linux server named `dev0`.

It uses native `k3s`, not `k3d`. On Linux, `k3s` can run directly on the server, so the Kubernetes `NodePort` services are reachable through the server IP without a k3d load balancer.

The setup starts:

- PostgreSQL
- RabbitMQ
- Redis
- Cassandra
- `identity-service`
- `group-service`
- `presence-service`
- `chat-service`
- `message-storage-service`

The current Kubernetes manifests are local/dev manifests. They are useful for a development server, but they are not production-ready because the databases and RabbitMQ are single-pod deployments with local persistent volumes and development secrets.

## 0. Assumptions

This guide assumes:

- The Linux server hostname is `dev0`.
- You have SSH access to `dev0`.
- `dev0` is reachable from your client machine.
- You can run `sudo` on `dev0`.
- The repo will live at `/opt/connect-chat`.
- You are deploying the current local Kubernetes manifests from `k8s/local`.

Replace these values if your environment is different:

```bash
export DEV0_HOST=dev0
export APP_DIR=/opt/connect-chat
```

If DNS for `dev0` is not set up, use the server IP instead:

```bash
export DEV0_HOST=192.0.2.10
```

## 1. Server sizing

Use at least:

```text
CPU:    4 cores
RAM:    8 GB minimum, 10-12 GB preferred
Disk:   30 GB free minimum
OS:     Ubuntu/Debian-style Linux is assumed in the commands below
```

Cassandra is the heaviest local dependency. If the Kubernetes API starts timing out, or RabbitMQ/Cassandra probes fail repeatedly, increase RAM first.

Do not run the repo's `docker compose` infrastructure stack at the same time as the k3s stack. That duplicates Postgres, RabbitMQ, Redis, and Cassandra and can exhaust memory quickly.

## 2. Install base packages

SSH into the server:

```bash
ssh "$DEV0_HOST"
```

Install common tools:

```bash
sudo apt-get update
sudo apt-get install -y \
  ca-certificates \
  curl \
  git \
  gnupg \
  lsb-release \
  tar \
  gzip
```

## 3. Install Docker Engine

Docker is used here only to build the service images. Native k3s runs workloads with containerd, so after building the images you will import them into the k3s containerd image store.

Install Docker:

```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```

Add the Docker apt repository:

```bash
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
```

Install Docker:

```bash
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

Allow your user to run Docker commands without `sudo`:

```bash
sudo usermod -aG docker "$USER"
newgrp docker
```

Verify:

```bash
docker version
docker info
```

## 4. Install k3s

Install a single-node k3s server:

```bash
curl -sfL https://get.k3s.io | sh -
```

Wait for the node:

```bash
sudo k3s kubectl get nodes
```

Expected:

```text
NAME   STATUS   ROLES                  VERSION
dev0   Ready    control-plane,master   ...
```

Set up `kubectl` for your shell:

```bash
mkdir -p "$HOME/.kube"
sudo cp /etc/rancher/k3s/k3s.yaml "$HOME/.kube/config"
sudo chown "$USER:$USER" "$HOME/.kube/config"
chmod 600 "$HOME/.kube/config"
```

Verify:

```bash
kubectl get nodes
kubectl get pods -A
```

## 5. Open firewall ports

The manifests expose client-facing services as Kubernetes `NodePort`s:

```text
identity-service   dev0:30081
group-service      dev0:30082
chat-service       dev0:30083
```

If `ufw` is enabled, allow these ports:

```bash
sudo ufw allow 30081/tcp
sudo ufw allow 30082/tcp
sudo ufw allow 30083/tcp
```

If you also use the optional reverse proxy later in this guide, allow:

```bash
sudo ufw allow 8081/tcp
sudo ufw allow 8082/tcp
sudo ufw allow 8083/tcp
```

Check firewall status:

```bash
sudo ufw status
```

If your server is behind a cloud firewall or campus firewall, open the same ports there too.

## 6. Clone the repository

Create the app directory:

```bash
sudo mkdir -p /opt/connect-chat
sudo chown "$USER:$USER" /opt/connect-chat
```

Clone the repo:

```bash
git clone <REPO_URL> /opt/connect-chat
cd /opt/connect-chat
```

If the repo is already present:

```bash
cd /opt/connect-chat
git pull
```

Confirm the manifests exist:

```bash
ls -la k8s/local
```

You should see:

```text
00-namespace.yaml
01-config.yaml
02-secret-local.yaml
10-postgres.yaml
11-rabbitmq.yaml
12-redis.yaml
13-cassandra.yaml
20-app-services.yaml
README.md
```

## 7. Stop duplicate Docker Compose infrastructure

If someone previously started the Docker Compose stack on `dev0`, stop it before starting k3s workloads:

```bash
cd /opt/connect-chat
docker compose ps
docker compose stop
```

This stops the duplicate local infrastructure containers but keeps their named volumes. Do not run `docker compose up -d` for this k3s setup.

## 8. Build service images

Build all five Spring Boot service images:

```bash
cd /opt/connect-chat

docker build -f identity-service/.docker/Dockerfile \
  -t connect-chat/identity-service:local \
  identity-service

docker build -f group-service/.docker/Dockerfile \
  -t connect-chat/group-service:local \
  group-service

docker build -f chat-service/.docker/Dockerfile \
  -t connect-chat/chat-service:local \
  chat-service

docker build -f message-storage-service/.docker/Dockerfile \
  -t connect-chat/message-storage-service:local \
  message-storage-service

docker build -f presence-service/.docker/Dockerfile \
  -t connect-chat/presence-service:local \
  presence-service
```

Verify the images:

```bash
docker images 'connect-chat/*'
```

Expected tags:

```text
connect-chat/identity-service:local
connect-chat/group-service:local
connect-chat/chat-service:local
connect-chat/message-storage-service:local
connect-chat/presence-service:local
```

## 9. Import images into k3s

k3s uses containerd, not Docker, to run pods. The images built by Docker must be imported into k3s.

Save the images to a tarball:

```bash
docker save \
  connect-chat/identity-service:local \
  connect-chat/group-service:local \
  connect-chat/chat-service:local \
  connect-chat/message-storage-service:local \
  connect-chat/presence-service:local \
  -o /tmp/connect-chat-images.tar
```

Import the tarball into k3s containerd:

```bash
sudo k3s ctr images import /tmp/connect-chat-images.tar
```

Verify k3s can see them:

```bash
sudo k3s ctr images ls | grep connect-chat
```

The Kubernetes manifests use `imagePullPolicy: IfNotPresent`, so the pods should use these locally imported images.

## 10. Review local secrets

The local secret file is:

```text
k8s/local/02-secret-local.yaml
```

It contains development values such as:

```text
POSTGRES_PASSWORD: postgres
RABBITMQ_DEFAULT_PASS: guest
IDENTITY_JWT_SECRET: connect-chat-local-jwt-secret-must-be-at-least-32-bytes
```

For a shared dev server, change these before applying the manifests:

```bash
vim k8s/local/02-secret-local.yaml
```

At minimum, change:

```text
POSTGRES_PASSWORD
IDENTITY_DB_PASSWORD
IDENTITY_FLYWAY_PASSWORD
GROUP_DB_PASSWORD
GROUP_FLYWAY_PASSWORD
CHAT_DB_PASSWORD
CHAT_FLYWAY_PASSWORD
MESSAGE_STORAGE_DB_PASSWORD
MESSAGE_STORAGE_FLYWAY_PASSWORD
RABBITMQ_DEFAULT_PASS
CHAT_RABBITMQ_PASSWORD
MESSAGE_STORAGE_RABBITMQ_PASSWORD
IDENTITY_JWT_SECRET
IDENTITY_INTERNAL_CLIENT_CHAT_SERVICE_SECRET
CHAT_SERVICE_CLIENT_SECRET
```

Keep matching values in sync. For example, if you change `RABBITMQ_DEFAULT_PASS`, also change `CHAT_RABBITMQ_PASSWORD` and `MESSAGE_STORAGE_RABBITMQ_PASSWORD`.

If this is only an isolated throwaway `dev0`, the committed local values are enough to validate the stack.

## 11. Apply Kubernetes manifests

Apply the namespace first:

```bash
kubectl apply -f k8s/local/00-namespace.yaml
```

Apply the rest:

```bash
kubectl apply -f k8s/local
```

Check the namespace:

```bash
kubectl get namespace connect-chat
```

## 12. Wait for infrastructure

Wait for Postgres:

```bash
kubectl -n connect-chat rollout status deploy/postgres --timeout=180s
```

Wait for Redis:

```bash
kubectl -n connect-chat rollout status deploy/redis --timeout=120s
```

Wait for RabbitMQ:

```bash
kubectl -n connect-chat rollout status deploy/rabbitmq --timeout=240s
```

Wait for Cassandra:

```bash
kubectl -n connect-chat rollout status deploy/cassandra --timeout=420s
```

Wait for the Cassandra initialization job:

```bash
kubectl -n connect-chat wait --for=condition=complete job/cassandra-init --timeout=420s
```

Cassandra can take several minutes on a small server.

## 13. Wait for application services

Wait for identity:

```bash
kubectl -n connect-chat rollout status deploy/identity-service --timeout=180s
```

Wait for presence:

```bash
kubectl -n connect-chat rollout status deploy/presence-service --timeout=180s
```

Wait for group:

```bash
kubectl -n connect-chat rollout status deploy/group-service --timeout=180s
```

Wait for chat:

```bash
kubectl -n connect-chat rollout status deploy/chat-service --timeout=180s
```

Wait for message storage:

```bash
kubectl -n connect-chat rollout status deploy/message-storage-service --timeout=180s
```

## 14. Verify pods and services

Check pods:

```bash
kubectl -n connect-chat get pods
```

Expected result:

```text
postgres                  1/1 Running
redis                     1/1 Running
rabbitmq                  1/1 Running
cassandra                 1/1 Running
cassandra-init            Completed
identity-service          1/1 Running
presence-service          1/1 Running
group-service             1/1 Running
chat-service              3/3 Running
message-storage-service   1/1 Running
```

Check services:

```bash
kubectl -n connect-chat get svc
```

Important exposed services:

```text
identity-service   NodePort   8081:30081/TCP
group-service      NodePort   8082:30082/TCP
chat-service       NodePort   8083:30083/TCP
```

## 15. Access from a client

With the current manifests, use the Kubernetes NodePorts directly:

```text
Identity: http://dev0:30081
Group:    http://dev0:30082
Chat:     ws://dev0:30083/ws/chat
```

Health checks:

```bash
curl "http://$DEV0_HOST:30081/actuator/health"
curl "http://$DEV0_HOST:30082/actuator/health"
```

Expected:

```json
{"groups":["liveness","readiness"],"status":"UP"}
```

If you want the same ports used by the local k3d setup, add the optional reverse proxy in the next section.

## 16. Optional: expose friendly ports 8081-8083

The Kubernetes services expose `30081`, `30082`, and `30083` because Kubernetes NodePorts must be in the NodePort range. If your client expects `8081`, `8082`, and `8083`, put Nginx on the host as a reverse proxy.

Install Nginx:

```bash
sudo apt-get install -y nginx
```

Create an Nginx config:

```bash
sudo tee /etc/nginx/sites-available/connect-chat-dev0 >/dev/null <<'EOF'
server {
    listen 8081;
    server_name _;

    location / {
        proxy_pass http://127.0.0.1:30081;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 8082;
    server_name _;

    location / {
        proxy_pass http://127.0.0.1:30082;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 8083;
    server_name _;

    location / {
        proxy_pass http://127.0.0.1:30083;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
```

Enable it:

```bash
sudo ln -sf /etc/nginx/sites-available/connect-chat-dev0 /etc/nginx/sites-enabled/connect-chat-dev0
sudo nginx -t
sudo systemctl reload nginx
```

Now client-facing URLs are:

```text
Identity: http://dev0:8081
Group:    http://dev0:8082
Chat:     ws://dev0:8083/ws/chat
```

Verify:

```bash
curl "http://$DEV0_HOST:8081/actuator/health"
curl "http://$DEV0_HOST:8082/actuator/health"
```

## 17. Optional: access internal tools

The following services are intentionally `ClusterIP` only:

```text
postgres
rabbitmq
redis
cassandra
presence-service
message-storage-service
```

Use `kubectl port-forward` from the server or from a machine with kubeconfig access.

RabbitMQ management UI:

```bash
kubectl -n connect-chat port-forward svc/rabbitmq 15672:15672
```

Then open:

```text
http://localhost:15672
```

Default local credentials, unless changed:

```text
guest / guest
```

Message storage service:

```bash
kubectl -n connect-chat port-forward svc/message-storage-service 8084:8084
```

Presence service:

```bash
kubectl -n connect-chat port-forward svc/presence-service 8085:8085
```

Postgres:

```bash
kubectl -n connect-chat port-forward svc/postgres 5432:5432
```

Cassandra:

```bash
kubectl -n connect-chat port-forward svc/cassandra 9042:9042
```

## 18. Logs and debugging

Show all pods:

```bash
kubectl -n connect-chat get pods -o wide
```

Describe a pod:

```bash
kubectl -n connect-chat describe pod <POD_NAME>
```

Follow logs:

```bash
kubectl -n connect-chat logs deploy/identity-service -f
kubectl -n connect-chat logs deploy/group-service -f
kubectl -n connect-chat logs deploy/chat-service -f
kubectl -n connect-chat logs deploy/message-storage-service -f
kubectl -n connect-chat logs deploy/rabbitmq -f
kubectl -n connect-chat logs deploy/cassandra -f
```

Check events:

```bash
kubectl -n connect-chat get events --sort-by=.lastTimestamp
```

Check chat replicas:

```bash
kubectl -n connect-chat get pods -l app.kubernetes.io/name=chat-service
```

Check Redis presence keys:

```bash
kubectl -n connect-chat exec deploy/redis -- redis-cli KEYS '*'
```

Check detailed presence session records:

```bash
kubectl -n connect-chat exec deploy/redis -- sh -c 'for key in $(redis-cli --scan --pattern "presence:session:*"); do echo "$key"; redis-cli HGETALL "$key"; done'
```

Check Cassandra messages:

```bash
kubectl -n connect-chat exec deploy/cassandra -- \
  cqlsh -e "SELECT * FROM connect_chat.messages_by_id;"
```

## 19. Rebuild and redeploy after code changes

When code changes, rebuild the changed service image.

Example for `identity-service`:

```bash
cd /opt/connect-chat

docker build -f identity-service/.docker/Dockerfile \
  -t connect-chat/identity-service:local \
  identity-service
```

Save and import the updated image:

```bash
docker save connect-chat/identity-service:local -o /tmp/identity-service.tar
sudo k3s ctr images import /tmp/identity-service.tar
```

Restart the deployment:

```bash
kubectl -n connect-chat rollout restart deploy/identity-service
kubectl -n connect-chat rollout status deploy/identity-service --timeout=180s
```

Repeat the same pattern for any other service:

```bash
kubectl -n connect-chat rollout restart deploy/group-service
kubectl -n connect-chat rollout restart deploy/chat-service
kubectl -n connect-chat rollout restart deploy/message-storage-service
kubectl -n connect-chat rollout restart deploy/presence-service
```

## 20. Reset the dev stack

To delete all workloads and local persistent volume claims:

```bash
kubectl delete namespace connect-chat
```

Then recreate:

```bash
kubectl apply -f k8s/local/00-namespace.yaml
kubectl apply -f k8s/local
```

If only Cassandra initialization needs to be rerun:

```bash
kubectl -n connect-chat delete job cassandra-init
kubectl apply -f k8s/local/13-cassandra.yaml
```

If k3s itself needs a full reset:

```bash
sudo /usr/local/bin/k3s-uninstall.sh
curl -sfL https://get.k3s.io | sh -
```

After reinstalling k3s, repeat the kubeconfig setup, image import, and manifest apply steps.

## 21. Common problems

### `ImagePullBackOff`

Check the pod:

```bash
kubectl -n connect-chat describe pod <POD_NAME>
```

If the event says the image cannot be pulled, import the local images into k3s:

```bash
sudo k3s ctr images ls | grep connect-chat
sudo k3s ctr images import /tmp/connect-chat-images.tar
kubectl -n connect-chat rollout restart deploy/<DEPLOYMENT_NAME>
```

### Kubernetes API timeouts

Check memory:

```bash
free -h
```

Check for duplicate Docker Compose containers:

```bash
docker compose ps
docker ps
```

Stop the duplicate compose stack:

```bash
docker compose stop
```

### RabbitMQ starts but never becomes ready

Check logs:

```bash
kubectl -n connect-chat logs deploy/rabbitmq --tail=200
```

Check events:

```bash
kubectl -n connect-chat describe deploy/rabbitmq
kubectl -n connect-chat get events --sort-by=.lastTimestamp
```

The local manifest sets RabbitMQ probe timeouts to `10` seconds. If a smaller timeout reappears, re-apply the manifest:

```bash
kubectl apply -f k8s/local/11-rabbitmq.yaml
```

### Cassandra takes a long time

This is expected on small machines. Wait for:

```bash
kubectl -n connect-chat rollout status deploy/cassandra --timeout=420s
kubectl -n connect-chat wait --for=condition=complete job/cassandra-init --timeout=420s
```

If it still fails, inspect logs:

```bash
kubectl -n connect-chat logs deploy/cassandra --tail=200
kubectl -n connect-chat logs job/cassandra-init --tail=200
```

### Client cannot reach services

Verify Kubernetes services:

```bash
kubectl -n connect-chat get svc identity-service group-service chat-service
```

Verify from the server:

```bash
curl http://127.0.0.1:30081/actuator/health
curl http://127.0.0.1:30082/actuator/health
```

Verify from your client:

```bash
curl "http://$DEV0_HOST:30081/actuator/health"
curl "http://$DEV0_HOST:30082/actuator/health"
```

If server-local curl works but client curl fails, check firewalls.

## 22. Current multi-pod chat limitation

The local setup runs `chat-service` with `3` replicas and Kubernetes distributes new WebSocket connections across those pods.

Private message delivery is not fully multi-instance safe yet. The current chat-service RabbitMQ listener uses one shared queue, so one arbitrary chat pod consumes each message event. If that pod is not the pod holding the sender or recipient WebSocket session, `convertAndSendToUser(...)` will not reach that user.

The production-ready direction is to route delivery events to the specific chat pod recorded in presence, usually with per-instance queues or routing keys based on `CHAT_SERVICE_INSTANCE_ID`.

## 23. Quick command summary

From a prepared `dev0` server:

```bash
cd /opt/connect-chat

docker compose stop

docker build -f identity-service/.docker/Dockerfile -t connect-chat/identity-service:local identity-service
docker build -f group-service/.docker/Dockerfile -t connect-chat/group-service:local group-service
docker build -f chat-service/.docker/Dockerfile -t connect-chat/chat-service:local chat-service
docker build -f message-storage-service/.docker/Dockerfile -t connect-chat/message-storage-service:local message-storage-service
docker build -f presence-service/.docker/Dockerfile -t connect-chat/presence-service:local presence-service

docker save \
  connect-chat/identity-service:local \
  connect-chat/group-service:local \
  connect-chat/chat-service:local \
  connect-chat/message-storage-service:local \
  connect-chat/presence-service:local \
  -o /tmp/connect-chat-images.tar

sudo k3s ctr images import /tmp/connect-chat-images.tar

kubectl apply -f k8s/local/00-namespace.yaml
kubectl apply -f k8s/local

kubectl -n connect-chat rollout status deploy/postgres --timeout=180s
kubectl -n connect-chat rollout status deploy/redis --timeout=120s
kubectl -n connect-chat rollout status deploy/rabbitmq --timeout=240s
kubectl -n connect-chat rollout status deploy/cassandra --timeout=420s
kubectl -n connect-chat wait --for=condition=complete job/cassandra-init --timeout=420s

kubectl -n connect-chat rollout status deploy/identity-service --timeout=180s
kubectl -n connect-chat rollout status deploy/presence-service --timeout=180s
kubectl -n connect-chat rollout status deploy/group-service --timeout=180s
kubectl -n connect-chat rollout status deploy/chat-service --timeout=180s
kubectl -n connect-chat rollout status deploy/message-storage-service --timeout=180s

kubectl -n connect-chat get pods
kubectl -n connect-chat get svc
```

Client URLs with direct NodePorts:

```text
http://dev0:30081
http://dev0:30082
ws://dev0:30083/ws/chat
```

Client URLs with optional Nginx reverse proxy:

```text
http://dev0:8081
http://dev0:8082
ws://dev0:8083/ws/chat
```
