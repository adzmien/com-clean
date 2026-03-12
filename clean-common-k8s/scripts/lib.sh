#!/usr/bin/env bash

DEFAULT_NAMESPACE="com-clean-dev"
DEFAULT_DB_SERVICE_MODE="nodeport"
DEFAULT_ENV="dev"
VALID_ACTIONS=("deploy" "destroy" "status" "dry-run" "print-config")
VALID_COMPONENTS=("all" "db" "redis" "infinispan")
VALID_DB_SERVICE_MODES=("nodeport" "clusterip")
VALID_INFINISPAN_SERVICE_MODES=("nodeport" "clusterip" "prod-clusterip")

# Shared utility functions for k8s automation scripts.
die() {
  echo "ERROR: $*" >&2
  exit 1
}

contains() {
  local needle="$1"
  shift

  local value
  for value in "$@"; do
    if [[ "$value" == "$needle" ]]; then
      return 0
    fi
  done

  return 1
}

validate_value() {
  local value="$1"
  local label="$2"
  shift 2
  local allowed=("$@")

  contains "$value" "${allowed[@]}" || die "Invalid ${label}: ${value}. Allowed: ${allowed[*]}"
}

require_cmd() {
  local command_name="$1"
  command -v "$command_name" >/dev/null 2>&1 || die "Required command not found: ${command_name}"
}

load_env_profile() {
  ENV="${ENV:-$DEFAULT_ENV}"

  # Restrict ENV profile names to safe path segments only.
  [[ "$ENV" =~ ^[a-z0-9][a-z0-9-]*$ ]] || die "Invalid ENV: ${ENV}. Use lowercase letters, numbers, and hyphens only."

  local profile_file="${ROOT_DIR}/environments/${ENV}.env"
  [[ -f "$profile_file" ]] || die "Missing environment profile: ${profile_file}"
  ENV_PROFILE_FILE="$profile_file"

  # shellcheck disable=SC1090
  source "$profile_file"

  [[ -n "${ENV_NAMESPACE:-}" ]] || die "ENV_NAMESPACE is required in ${profile_file}"
  [[ -n "${ENV_DB_SERVICE_MODE:-}" ]] || die "ENV_DB_SERVICE_MODE is required in ${profile_file}"
}

setup_kubectl_args() {
  KUBECTL_CONTEXT_ARGS=""
  if [[ -n "${KUBE_CONTEXT:-}" ]]; then
    KUBECTL_CONTEXT_ARGS="${KUBE_CONTEXT}"
  fi
}

k() {
  if [[ -n "${KUBECTL_CONTEXT_ARGS:-}" ]]; then
    kubectl --context "${KUBECTL_CONTEXT_ARGS}" "$@"
    return 0
  fi

  kubectl "$@"
}

rewrite_namespace_if_needed() {
  local target_namespace="$1"

  if [[ "$target_namespace" == "$DEFAULT_NAMESPACE" ]]; then
    cat
    return 0
  fi

  awk -v ns="$target_namespace" '
    /^[[:space:]]*namespace:[[:space:]]*/ {
      sub(/namespace:[[:space:]]*.*/, "namespace: " ns)
      print
      next
    }
    /infinispan-headless\.[^.]+\.svc/ {
      sub(/infinispan-headless\.[^.]+\.svc/, "infinispan-headless." ns ".svc")
      print
      next
    }
    { print }
  '
}
