# Local k3s Setup

These manifests run the app services and local development infrastructure inside a k3s/k3d cluster. They are intended for local validation only, not production.

## 1. Create a local cluster

On macOS, use k3d because native k3s is Linux-only:

```bash
k3d cluster create connect-chat \
  -p "8081:30081@loadbalancer" \
  -p "8082:30082@loadbalancer" \
  -p "8083:30083@loadbalancer"
kubectl config use-context k3d-connect-chat
```

The identity, group, and chat services are exposed through stable local k3d port mappings. The `8083:30083` port mapping exposes the chat `NodePort` through k3d so new WebSocket connections to `localhost:8083` are distributed across chat pods by Kubernetes.

## 2. Build and import service images

```bash
docker build -f identity-service/.docker/Dockerfile -t connect-chat/identity-service:local identity-service
docker build -f group-service/.docker/Dockerfile -t connect-chat/group-service:local group-service
docker build -f chat-service/.docker/Dockerfile -t connect-chat/chat-service:local chat-service
docker build -f message-storage-service/.docker/Dockerfile -t connect-chat/message-storage-service:local message-storage-service
docker build -f presence-service/.docker/Dockerfile -t connect-chat/presence-service:local presence-service
docker build -f ai-service/Dockerfile -t connect-chat/ai-service:local ai-service
docker build -f ride-and-park-backend/Dockerfile -t connect-chat/ride-and-park-backend:local ride-and-park-backend
docker build -f ride-and-park-mcp-server/Dockerfile -t connect-chat/ride-and-park-mcp-server:local ride-and-park-mcp-server

k3d image import connect-chat/identity-service:local -c connect-chat
k3d image import connect-chat/group-service:local -c connect-chat
k3d image import connect-chat/chat-service:local -c connect-chat
k3d image import connect-chat/message-storage-service:local -c connect-chat
k3d image import connect-chat/presence-service:local -c connect-chat
k3d image import connect-chat/ai-service:local -c connect-chat
k3d image import connect-chat/ride-and-park-backend:local -c connect-chat
k3d image import connect-chat/ride-and-park-mcp-server:local -c connect-chat
```

## 3. Apply manifests

Create the namespace first:

```bash
kubectl apply -f k8s/local/00-namespace.yaml
```

For secrets, either apply the local placeholder Secret and then override it from your ignored `.env` file:

```bash
kubectl apply -f k8s/local/02-secret-local.yaml
kubectl -n connect-chat create secret generic connect-chat-secret \
  --from-env-file=.env \
  --dry-run=client \
  -o yaml | kubectl apply -f -
```

Or keep local k3s secrets in ignored `k8s/local/02-secret-local.override.yaml`. This is the recommended path for `GOOGLE_API_KEY`, which is intentionally not stored in the tracked placeholder Secret. Copy `k8s/local/02-secret-local.yaml` to that path, change local-only values, and apply it after the placeholder Secret:

```bash
kubectl apply -f k8s/local/02-secret-local.yaml
kubectl apply -f k8s/local/02-secret-local.override.yaml
```

At minimum, set `GOOGLE_API_KEY` before using the AI bot. If you use the `.env` option, keep the existing local database/Rabbit/JWT keys there too so the generated `connect-chat-secret` remains complete.

Then apply the non-secret manifests:

```bash
kubectl apply -f k8s/local/01-config.yaml
kubectl apply -f k8s/local/10-postgres.yaml
kubectl apply -f k8s/local/11-rabbitmq.yaml
kubectl apply -f k8s/local/12-redis.yaml
kubectl apply -f k8s/local/13-cassandra.yaml
kubectl apply -f k8s/local/20-app-services.yaml
```

For quick local resets, applying the full directory is fine when you use `02-secret-local.override.yaml`, because the ignored override file lives in the same directory. If you use the `.env` command instead, run the Secret command again after applying the full directory so the placeholder Secret does not leave `GOOGLE_API_KEY` empty:

```bash
kubectl apply -f k8s/local
kubectl -n connect-chat create secret generic connect-chat-secret \
  --from-env-file=.env \
  --dry-run=client \
  -o yaml | kubectl apply -f -
```

Wait for infrastructure and apps:

```bash
kubectl -n connect-chat rollout status deploy/postgres --timeout=180s
kubectl -n connect-chat rollout status deploy/rabbitmq --timeout=180s
kubectl -n connect-chat rollout status deploy/redis --timeout=120s
kubectl -n connect-chat rollout status deploy/cassandra --timeout=420s
kubectl -n connect-chat wait --for=condition=complete job/cassandra-init --timeout=420s

kubectl -n connect-chat rollout status deploy/identity-service --timeout=180s
kubectl -n connect-chat rollout status deploy/presence-service --timeout=180s
kubectl -n connect-chat rollout status deploy/group-service --timeout=180s
kubectl -n connect-chat rollout status deploy/chat-service --timeout=180s
kubectl -n connect-chat rollout status deploy/message-storage-service --timeout=180s
kubectl -n connect-chat rollout status deploy/ride-and-park-backend --timeout=180s
kubectl -n connect-chat rollout status deploy/ride-and-park-mcp-server --timeout=180s
kubectl -n connect-chat rollout status deploy/ai-service --timeout=180s
```

## 4. Access services locally

Use the k3d port mapping for chat:

```bash
http://localhost:8081
http://localhost:8082
ws://localhost:8083/ws/chat
```

Run port-forward commands in separate terminals for the remaining services:

```bash
kubectl -n connect-chat port-forward svc/message-storage-service 8084:8084
kubectl -n connect-chat port-forward svc/presence-service 8085:8085
kubectl -n connect-chat port-forward svc/ai-service 8000:8000
kubectl -n connect-chat port-forward svc/ride-and-park-backend 3000:3000
kubectl -n connect-chat port-forward svc/ride-and-park-mcp-server 8080:8080
```

Do not use `kubectl port-forward svc/chat-service 8083:8083` when testing load distribution. Port-forwarding a service can pin traffic to a single backend pod instead of exercising normal Kubernetes service balancing.

Optional infrastructure access:

```bash
kubectl -n connect-chat port-forward svc/postgres 5432:5432
kubectl -n connect-chat port-forward svc/rabbitmq 15672:15672
kubectl -n connect-chat port-forward svc/cassandra 9042:9042
```

RabbitMQ management is available at `http://localhost:15672` with `guest` / `guest`.

## Useful checks

```bash
kubectl -n connect-chat get pods
kubectl -n connect-chat get svc
kubectl -n connect-chat logs deploy/chat-service -f
kubectl -n connect-chat logs deploy/message-storage-service -f
kubectl -n connect-chat logs deploy/ai-service -f
```

Check that three chat pods are running:

```bash
kubectl -n connect-chat get pods -l app.kubernetes.io/name=chat-service
```

Check which chat pod each WebSocket connection registered in presence:

```bash
kubectl -n connect-chat exec deploy/redis -- sh -c 'for key in $(redis-cli --scan --pattern "presence:session:*"); do echo "$key"; redis-cli HGETALL "$key"; done'
```

## Current multi-pod limitation

This setup distributes new WebSocket connections across three chat pods, but private message delivery is not fully multi-instance safe yet. The current chat-service RabbitMQ listener uses one shared queue, so one arbitrary chat pod consumes each message event. If that pod is not the pod holding the sender or recipient WebSocket session, `convertAndSendToUser(...)` will not reach that user.

The next production-ready step is to route delivery events to the specific chat pod recorded in presence, usually with per-instance queues or routing keys based on `CHAT_SERVICE_INSTANCE_ID`.

Check Cassandra messages:

```bash
kubectl -n connect-chat exec deploy/cassandra -- cqlsh -e "SELECT * FROM connect_chat.messages_by_id;"
```

Check Redis presence keys:

```bash
kubectl -n connect-chat exec deploy/redis -- redis-cli KEYS '*'
```

## Reset local data

Deleting the namespace removes the workloads and local PVC data:

```bash
kubectl delete namespace connect-chat
```

If you only want to rerun the Cassandra init job:

```bash
kubectl -n connect-chat delete job cassandra-init
kubectl apply -f k8s/local/13-cassandra.yaml
```

## Before using this on a real server

Replace the local Secret values, use proper storage classes/backups, add Ingress/TLS, and strongly consider managed or operator-based Postgres, RabbitMQ, Redis, and Cassandra instead of these single-pod dev manifests.
