#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

NAMESPACE="${NAMESPACE:-connect-chat}"
IMAGE_TAR="${IMAGE_TAR:-/tmp/connect-chat-images.tar}"
SKIP_BUILD="${SKIP_BUILD:-0}"
SKIP_IMPORT="${SKIP_IMPORT:-0}"
SKIP_WAIT="${SKIP_WAIT:-0}"
STOP_COMPOSE_CONTAINERS="${STOP_COMPOSE_CONTAINERS:-1}"

SERVICES=(
  identity-service
  group-service
  chat-service
  message-storage-service
  presence-service
)

IMAGES=(
  connect-chat/identity-service:local
  connect-chat/group-service:local
  connect-chat/chat-service:local
  connect-chat/message-storage-service:local
  connect-chat/presence-service:local
)

log() {
  printf '\n==> %s\n' "$*"
}

fail() {
  printf 'ERROR: %s\n' "$*" >&2
  exit 1
}

find_k3s() {
  if command -v k3s >/dev/null 2>&1; then
    command -v k3s
  elif [ -x /usr/local/bin/k3s ]; then
    printf '%s\n' /usr/local/bin/k3s
  else
    return 1
  fi
}

choose_docker() {
  if docker version >/dev/null 2>&1; then
    DOCKER_CMD=(docker)
    return
  fi

  if command -v sudo >/dev/null 2>&1; then
    log "Docker is not available to this user; using sudo for Docker commands."
    sudo -v
    DOCKER_CMD=(sudo docker)
    "${DOCKER_CMD[@]}" version >/dev/null
    return
  fi

  fail "Docker is installed but not usable, and sudo is unavailable."
}

choose_kubectl() {
  K3S_BIN="$(find_k3s)" || fail "k3s is not installed. Install k3s first, then rerun this script."
  if [ "$(id -u)" -eq 0 ]; then
    K3S_ROOT_CMD=("$K3S_BIN")
  else
    sudo -v
    K3S_ROOT_CMD=(sudo "$K3S_BIN")
  fi

  if command -v kubectl >/dev/null 2>&1 &&
    kubectl version --client >/dev/null 2>&1 &&
    kubectl get nodes >/dev/null 2>&1; then
    KUBECTL_CMD=(kubectl)
    return
  fi

  log "kubectl is not configured for this user; using sudo k3s kubectl."

  if [ "$(id -u)" -eq 0 ]; then
    KUBECTL_CMD=("$K3S_BIN" kubectl)
  else
    KUBECTL_CMD=(sudo "$K3S_BIN" kubectl)
  fi

  "${KUBECTL_CMD[@]}" version --client >/dev/null
}

stop_compose_containers() {
  [ "$STOP_COMPOSE_CONTAINERS" = "1" ] || return 0

  local containers=(
    connect-chat-postgres
    connect-chat-pgadmin
    connect-chat-redis
    connect-chat-cassandra
    connect-chat-cassandra-init
    connect-chat-rabbitmq
  )

  log "Stopping duplicate Docker Compose infrastructure containers if they are running"
  for container in "${containers[@]}"; do
    if "${DOCKER_CMD[@]}" ps -q --filter "name=^/${container}$" | grep -q .; then
      "${DOCKER_CMD[@]}" stop "$container"
    fi
  done
}

build_images() {
  [ "$SKIP_BUILD" = "1" ] && return 0

  log "Building service images"
  for service in "${SERVICES[@]}"; do
    "${DOCKER_CMD[@]}" build \
      -f "$service/.docker/Dockerfile" \
      -t "connect-chat/${service}:local" \
      "$service"
  done
}

import_images() {
  [ "$SKIP_IMPORT" = "1" ] && return 0

  log "Saving service images to $IMAGE_TAR"
  "${DOCKER_CMD[@]}" save "${IMAGES[@]}" -o "$IMAGE_TAR"

  log "Importing service images into k3s containerd"
  "${K3S_ROOT_CMD[@]}" ctr images import "$IMAGE_TAR"
}

apply_manifests() {
  log "Applying Kubernetes manifests"
  "${KUBECTL_CMD[@]}" apply -f k8s/local/00-namespace.yaml
  "${KUBECTL_CMD[@]}" apply -f k8s/local

  if [ -f k8s/local/02-secret-local.override.yaml ]; then
    log "Applying ignored local secret override"
    "${KUBECTL_CMD[@]}" apply -f k8s/local/02-secret-local.override.yaml
  fi
}

wait_for_stack() {
  [ "$SKIP_WAIT" = "1" ] && return 0

  log "Waiting for infrastructure"
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/postgres --timeout=180s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/redis --timeout=120s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/rabbitmq --timeout=240s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/cassandra --timeout=420s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" wait --for=condition=complete job/cassandra-init --timeout=420s

  log "Waiting for application services"
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/identity-service --timeout=180s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/presence-service --timeout=180s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/group-service --timeout=180s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/chat-service --timeout=180s
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" rollout status deploy/message-storage-service --timeout=180s
}

show_result() {
  log "Pods"
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" get pods -o wide

  log "Services"
  "${KUBECTL_CMD[@]}" -n "$NAMESPACE" get svc

  cat <<EOF

Direct local URLs:
  Identity: http://localhost:30081
  Group:    http://localhost:30082
  Chat:     ws://localhost:30083/ws/chat

Useful health checks:
  curl http://localhost:30081/actuator/health
  curl http://localhost:30082/actuator/health
EOF
}

choose_docker
choose_kubectl
stop_compose_containers
build_images
import_images
apply_manifests
wait_for_stack
show_result
