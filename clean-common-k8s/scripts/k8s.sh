#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=scripts/lib.sh
source "${SCRIPT_DIR}/lib.sh"

ACTION="${1:-}"
COMPONENT="${2:-all}"

if [[ -z "$ACTION" ]]; then
  die "Missing ACTION. Usage: ./scripts/k8s.sh <deploy|destroy|status|dry-run|print-config> [all|db|redis|infinispan]"
fi

load_env_profile

K8S_NAMESPACE="${K8S_NAMESPACE:-$ENV_NAMESPACE}"
DB_SERVICE_MODE="${DB_SERVICE_MODE:-$ENV_DB_SERVICE_MODE}"
INFINISPAN_SERVICE_MODE="${INFINISPAN_SERVICE_MODE:-${ENV_INFINISPAN_SERVICE_MODE:-nodeport}}"
KUBE_CONTEXT="${KUBE_CONTEXT:-${ENV_KUBE_CONTEXT:-}}"

validate_value "$ACTION" "ACTION" "${VALID_ACTIONS[@]}"
validate_value "$COMPONENT" "COMPONENT" "${VALID_COMPONENTS[@]}"
validate_value "$DB_SERVICE_MODE" "DB_SERVICE_MODE" "${VALID_DB_SERVICE_MODES[@]}"
validate_value "$INFINISPAN_SERVICE_MODE" "INFINISPAN_SERVICE_MODE" "${VALID_INFINISPAN_SERVICE_MODES[@]}"

# Single manifests directory — environment is switched via overlay selection.
MANIFEST_DIR="${ROOT_DIR}/manifests"

DB_OVERLAY_DIR="${MANIFEST_DIR}/01-database/overlays/${DB_SERVICE_MODE}"
REDIS_DIR="${MANIFEST_DIR}/02-redis"
INFINISPAN_OVERLAY_DIR="${MANIFEST_DIR}/03-infinispan/overlays/${INFINISPAN_SERVICE_MODE}"

print_config() {
  cat <<EOF_CONFIG
ENV=${ENV}
ENV_PROFILE_FILE=${ENV_PROFILE_FILE}
K8S_NAMESPACE=${K8S_NAMESPACE}
MANIFEST_DIR=${MANIFEST_DIR}
DB_SERVICE_MODE=${DB_SERVICE_MODE}
INFINISPAN_SERVICE_MODE=${INFINISPAN_SERVICE_MODE}
KUBE_CONTEXT=${KUBE_CONTEXT}
EOF_CONFIG
}

if [[ "$ACTION" == "print-config" ]]; then
  print_config
  exit 0
fi

require_cmd kubectl
kubectl kustomize --help >/dev/null 2>&1 || die "kubectl kustomize support is required (kubectl v1.14+)."

validate_component_dirs() {
  local target="$1"
  case "$target" in
    db)
      [[ -d "$DB_OVERLAY_DIR" ]] || die "Missing DB overlay: ${DB_OVERLAY_DIR}"
      ;;
    redis)
      [[ -f "${REDIS_DIR}/kustomization.yaml" ]] || die "Missing Redis kustomization: ${REDIS_DIR}/kustomization.yaml"
      ;;
    infinispan)
      [[ -d "$INFINISPAN_OVERLAY_DIR" ]] || die "Missing Infinispan overlay: ${INFINISPAN_OVERLAY_DIR}"
      ;;
    all)
      validate_component_dirs db
      validate_component_dirs redis
      validate_component_dirs infinispan
      ;;
  esac
}

validate_component_dirs "$COMPONENT"

setup_kubectl_args

render_db() {
  kubectl kustomize "$DB_OVERLAY_DIR" | rewrite_namespace_if_needed "$K8S_NAMESPACE"
}

render_redis() {
  kubectl kustomize "$REDIS_DIR" | rewrite_namespace_if_needed "$K8S_NAMESPACE"
}

render_infinispan() {
  kubectl kustomize "$INFINISPAN_OVERLAY_DIR" | rewrite_namespace_if_needed "$K8S_NAMESPACE"
}

namespace_manifest() {
  cat <<EOF_NS
apiVersion: v1
kind: Namespace
metadata:
  name: ${K8S_NAMESPACE}
EOF_NS
}

apply_namespace() {
  namespace_manifest | k apply -f -
}

dry_run_namespace() {
  if [[ "$CLUSTER_REACHABLE" == "true" ]]; then
    namespace_manifest | k apply --dry-run=client --validate=false -f -
    return 0
  fi

  namespace_manifest >/dev/null
  echo "namespace/${K8S_NAMESPACE} manifest render ok (cluster unreachable, skipped kubectl dry-run)"
}

wait_for_db() {
  k rollout status statefulset/mariadb -n "$K8S_NAMESPACE" --timeout=300s
}

wait_for_redis() {
  k rollout status deployment/redis -n "$K8S_NAMESPACE" --timeout=300s
}

wait_for_infinispan() {
  k rollout status statefulset/infinispan -n "$K8S_NAMESPACE" --timeout=300s
}

wait_for_infinispan_cache_job() {
  k wait --for=condition=complete --timeout=300s job/infinispan-cache-create -n "$K8S_NAMESPACE"
}

apply_component() {
  local target="$1"

  case "$target" in
    db)
      render_db | k apply -f -
      wait_for_db
      ;;
    redis)
      render_redis | k apply -f -
      wait_for_redis
      ;;
    infinispan)
      render_infinispan | k apply -f -
      wait_for_infinispan
      wait_for_infinispan_cache_job
      ;;
    all)
      apply_component db
      apply_component redis
      apply_component infinispan
      ;;
  esac
}

dry_run_component() {
  local target="$1"

  case "$target" in
    db)
      if [[ "$CLUSTER_REACHABLE" == "true" ]]; then
        render_db | k apply --dry-run=client --validate=false -f -
      else
        render_db >/dev/null
        echo "db manifests render ok (cluster unreachable, skipped kubectl dry-run)"
      fi
      ;;
    redis)
      if [[ "$CLUSTER_REACHABLE" == "true" ]]; then
        render_redis | k apply --dry-run=client --validate=false -f -
      else
        render_redis >/dev/null
        echo "redis manifests render ok (cluster unreachable, skipped kubectl dry-run)"
      fi
      ;;
    infinispan)
      if [[ "$CLUSTER_REACHABLE" == "true" ]]; then
        render_infinispan | k apply --dry-run=client --validate=false -f -
      else
        render_infinispan >/dev/null
        echo "infinispan manifests render ok (cluster unreachable, skipped kubectl dry-run)"
      fi
      ;;
    all)
      dry_run_component db
      dry_run_component redis
      dry_run_component infinispan
      ;;
  esac
}

delete_component() {
  local target="$1"

  case "$target" in
    db)
      render_db | k delete --ignore-not-found=true -f -
      ;;
    redis)
      render_redis | k delete --ignore-not-found=true -f -
      ;;
    infinispan)
      render_infinispan | k delete --ignore-not-found=true -f -
      ;;
    all)
      # Delete in reverse dependency order.
      delete_component infinispan
      delete_component redis
      delete_component db
      ;;
  esac
}

check_namespace_state() {
  local output
  if output="$(k get namespace "$K8S_NAMESPACE" -o name 2>&1)"; then
    return 0
  fi

  if [[ "$output" == *"NotFound"* || "$output" == *"not found"* ]]; then
    echo "Namespace ${K8S_NAMESPACE} does not exist."
    return 2
  fi

  echo "Unable to query namespace ${K8S_NAMESPACE}: ${output}" >&2
  return 1
}

status_all() {
  check_namespace_state
  local status_code=$?
  if [[ "$status_code" -eq 2 ]]; then
    return 0
  fi
  [[ "$status_code" -eq 0 ]] || return "$status_code"

  k get all -n "$K8S_NAMESPACE"
  k get secret mariadb-secret redis-auth infinispan-secret -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get configmap mariadb-config -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get pvc -n "$K8S_NAMESPACE" --ignore-not-found=true
}

status_db() {
  check_namespace_state
  local status_code=$?
  if [[ "$status_code" -eq 2 ]]; then
    return 0
  fi
  [[ "$status_code" -eq 0 ]] || return "$status_code"

  k get statefulset mariadb -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get service mariadb -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get pods -n "$K8S_NAMESPACE" -l app=mariadb
  k get configmap mariadb-config -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get secret mariadb-secret -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get pvc -n "$K8S_NAMESPACE" --ignore-not-found=true
}

status_redis() {
  check_namespace_state
  local status_code=$?
  if [[ "$status_code" -eq 2 ]]; then
    return 0
  fi
  [[ "$status_code" -eq 0 ]] || return "$status_code"

  k get deployment redis -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get service redis -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get pods -n "$K8S_NAMESPACE" -l app=redis
  k get secret redis-auth -n "$K8S_NAMESPACE" --ignore-not-found=true
}

status_infinispan() {
  check_namespace_state
  local status_code=$?
  if [[ "$status_code" -eq 2 ]]; then
    return 0
  fi
  [[ "$status_code" -eq 0 ]] || return "$status_code"

  k get statefulset infinispan -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get service infinispan infinispan-headless -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get job infinispan-cache-create -n "$K8S_NAMESPACE" --ignore-not-found=true
  k get pods -n "$K8S_NAMESPACE" -l app=infinispan
  k get secret infinispan-secret -n "$K8S_NAMESPACE" --ignore-not-found=true
}

echo "ACTION=${ACTION} ENV=${ENV} COMPONENT=${COMPONENT} K8S_NAMESPACE=${K8S_NAMESPACE} DB_SERVICE_MODE=${DB_SERVICE_MODE} INFINISPAN_SERVICE_MODE=${INFINISPAN_SERVICE_MODE}${KUBE_CONTEXT:+ KUBE_CONTEXT=${KUBE_CONTEXT}}"

case "$ACTION" in
  deploy)
    apply_namespace
    apply_component "$COMPONENT"
    ;;
  dry-run)
    CLUSTER_REACHABLE="false"
    if k get --raw=/version >/dev/null 2>&1; then
      CLUSTER_REACHABLE="true"
    fi

    dry_run_namespace
    dry_run_component "$COMPONENT"
    ;;
  destroy)
    if [[ "$COMPONENT" == "all" ]]; then
      delete_component all
      k delete namespace "$K8S_NAMESPACE" --ignore-not-found=true
    else
      delete_component "$COMPONENT"
    fi
    ;;
  status)
    case "$COMPONENT" in
      all)
        status_all
        ;;
      db)
        status_db
        ;;
      redis)
        status_redis
        ;;
      infinispan)
        status_infinispan
        ;;
    esac
    ;;
esac
