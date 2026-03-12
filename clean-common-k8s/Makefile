SHELL := /usr/bin/env bash

ENV ?= dev
K8S_NAMESPACE ?=
DB_SERVICE_MODE ?=
KUBE_CONTEXT ?=

K8S_SCRIPT := ./scripts/k8s.sh
RUN_K8S := ENV="$(ENV)" K8S_NAMESPACE="$(K8S_NAMESPACE)" DB_SERVICE_MODE="$(DB_SERVICE_MODE)" KUBE_CONTEXT="$(KUBE_CONTEXT)" "$(K8S_SCRIPT)"

.PHONY: help print-config deploy deploy-db deploy-redis deploy-infinispan destroy destroy-db destroy-redis destroy-infinispan status status-db status-redis status-infinispan dry-run dry-run-db dry-run-redis dry-run-infinispan

help:
	@echo "Kubernetes automation targets"
	@echo "  make deploy           # Deploy namespace + db + redis + infinispan"
	@echo "  make deploy-db        # Deploy database only"
	@echo "  make deploy-redis     # Deploy redis only"
	@echo "  make deploy-infinispan # Deploy infinispan only"
	@echo "  make destroy          # Delete infinispan + redis + db + namespace"
	@echo "  make destroy-db       # Delete database only"
	@echo "  make destroy-redis    # Delete redis only"
	@echo "  make destroy-infinispan # Delete infinispan only"
	@echo "  make print-config     # Show resolved env/profile settings"
	@echo "  make status           # Show namespace workload status"
	@echo "  make status-infinispan # Show infinispan status"
	@echo "  make dry-run          # Client-side validation for all"
	@echo "  make dry-run-infinispan # Client-side validation for infinispan"
	@echo
	@echo "Environment variables"
	@echo "  ENV=$(ENV)  (<profile-name>, loaded from environments/<ENV>.env)"
	@echo "  K8S_NAMESPACE=$(if $(K8S_NAMESPACE),$(K8S_NAMESPACE),<from ENV profile>)"
	@echo "  DB_SERVICE_MODE=$(if $(DB_SERVICE_MODE),$(DB_SERVICE_MODE),<from ENV profile>)  (nodeport|clusterip)"
	@echo "  KUBE_CONTEXT=$(if $(KUBE_CONTEXT),$(KUBE_CONTEXT),<from ENV profile>)"

print-config:
	@$(RUN_K8S) print-config

deploy:
	@$(RUN_K8S) deploy all

deploy-db:
	@$(RUN_K8S) deploy db

deploy-redis:
	@$(RUN_K8S) deploy redis

deploy-infinispan:
	@$(RUN_K8S) deploy infinispan

destroy:
	@$(RUN_K8S) destroy all

destroy-db:
	@$(RUN_K8S) destroy db

destroy-redis:
	@$(RUN_K8S) destroy redis

destroy-infinispan:
	@$(RUN_K8S) destroy infinispan

status:
	@$(RUN_K8S) status all

status-db:
	@$(RUN_K8S) status db

status-redis:
	@$(RUN_K8S) status redis

status-infinispan:
	@$(RUN_K8S) status infinispan


dry-run:
	@$(RUN_K8S) dry-run all


dry-run-db:
	@$(RUN_K8S) dry-run db


dry-run-redis:
	@$(RUN_K8S) dry-run redis

dry-run-infinispan:
	@$(RUN_K8S) dry-run infinispan
