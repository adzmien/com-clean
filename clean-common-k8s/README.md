# clean-common-k8s

Kubernetes manifests and automation for the `com-clean` infrastructure.

**Single manifest directory, multiple environments** — switch by passing `ENV=<profile>`.

---

## Directory Structure

```
clean-common-k8s/
├── Makefile                        # Automation entry point
├── environments/
│   ├── dev.env                     # Development profile
│   ├── sit.env                     # SIT profile
│   └── prod.env                    # Production profile
├── scripts/
│   ├── lib.sh                      # Shared utilities + namespace rewriting
│   └── k8s.sh                      # Main orchestration script
└── manifests/                      # Single source of truth
    ├── 01-database/
    │   ├── base/                   # Secret, ConfigMap, StatefulSet
    │   └── overlays/
    │       ├── nodeport/           # MariaDB service as NodePort (30306) — dev/sit
    │       └── clusterip/          # MariaDB service as ClusterIP — prod
    ├── 02-redis/                   # Secret, Deployment, Service
    └── 03-infinispan/
        ├── base/                   # Secret, Services, StatefulSet, cache-create Job
        └── overlays/
            ├── nodeport/           # Infinispan service as NodePort (31767) — dev/sit
            ├── clusterip/          # Infinispan service as ClusterIP (base size)
            └── prod-clusterip/     # Infinispan ClusterIP + replicas:3 + large resources — prod
```

---

## Environment Profiles

| Profile | Namespace | DB Service | Infinispan Overlay | Infinispan Replicas |
|---------|-----------|------------|-------------------|---------------------|
| `dev`   | com-clean-dev  | NodePort (30306) | nodeport       | 1 |
| `sit`   | com-clean-sit  | NodePort (30306) | nodeport       | 1 |
| `prod`  | com-clean-prod | ClusterIP        | prod-clusterip | 3 |

---

## Prerequisites

- `kubectl` v1.14+ (requires built-in `kustomize` support)
- Access to target Kubernetes cluster
- Optional: set `KUBE_CONTEXT` to switch clusters

---

## Deploy with Makefile (Recommended)

### Validate before deploying (dry run)

```bash
# Validate all components against dev cluster
make dry-run

# Validate against prod (no cluster needed — renders manifests only)
make dry-run ENV=prod
```

### Deploy all components

```bash
# Deploy to dev (default)
make deploy

# Deploy to sit
make deploy ENV=sit

# Deploy to prod
make deploy ENV=prod
```

### Deploy individual components

```bash
make deploy-db            ENV=dev
make deploy-redis         ENV=dev
make deploy-infinispan    ENV=dev
```

### Check status

```bash
make status               ENV=dev
make status-db            ENV=dev
make status-redis         ENV=dev
make status-infinispan    ENV=prod
```

### Destroy

```bash
# Destroy all (infinispan → redis → db → namespace, in reverse order)
make destroy              ENV=dev

# Destroy single component
make destroy-infinispan   ENV=dev
make destroy-redis        ENV=dev
make destroy-db           ENV=dev
```

### Show resolved configuration

```bash
make print-config ENV=prod
```

Output:
```
ENV=prod
ENV_PROFILE_FILE=.../environments/prod.env
K8S_NAMESPACE=com-clean-prod
MANIFEST_DIR=.../manifests
DB_SERVICE_MODE=clusterip
INFINISPAN_SERVICE_MODE=prod-clusterip
KUBE_CONTEXT=
```

---

## Override Variables

You can override any env profile value on the command line:

```bash
# Override namespace
make deploy ENV=sit K8S_NAMESPACE=com-clean-stage

# Override DB service type
make deploy ENV=dev DB_SERVICE_MODE=clusterip

# Override infinispan overlay
make deploy ENV=dev INFINISPAN_SERVICE_MODE=prod-clusterip

# Target a specific kubectl context
make deploy ENV=prod KUBE_CONTEXT=arn:aws:eks:ap-southeast-1:123:cluster/my-cluster
```

---

## Add a New Environment

1. Create `environments/<env>.env`:
   ```properties
   ENV_NAMESPACE=com-clean-<env>
   ENV_DB_SERVICE_MODE=nodeport
   ENV_INFINISPAN_SERVICE_MODE=nodeport
   ENV_KUBE_CONTEXT=
   ```

2. Validate:
   ```bash
   make print-config ENV=<env>
   make dry-run ENV=<env>
   ```

3. Deploy:
   ```bash
   make deploy ENV=<env>
   ```

---

## Deploy with kubectl (Manual)

Render and apply manifests directly using `kubectl kustomize`:

```bash
NS=com-clean-dev

# Create namespace
kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: ${NS}
EOF

# Deploy database (NodePort)
kubectl kustomize manifests/01-database/overlays/nodeport \
  | sed "s/namespace: com-clean-dev/namespace: ${NS}/g" \
  | kubectl apply -f -

# Deploy Redis
kubectl kustomize manifests/02-redis \
  | sed "s/namespace: com-clean-dev/namespace: ${NS}/g" \
  | kubectl apply -f -

# Deploy Infinispan (NodePort)
kubectl kustomize manifests/03-infinispan/overlays/nodeport \
  | sed "s/namespace: com-clean-dev/namespace: ${NS}/g" \
  | kubectl apply -f -

# Deploy Infinispan (prod — ClusterIP + 3 replicas + large resources)
kubectl kustomize manifests/03-infinispan/overlays/prod-clusterip \
  | sed "s/namespace: com-clean-dev/namespace: com-clean-prod/g; \
         s/infinispan-headless\.com-clean-dev\.svc/infinispan-headless.com-clean-prod.svc/g" \
  | kubectl apply -f -
```

---

## Common kubectl Commands

### Cluster & Context

```bash
# Show current context
kubectl config current-context

# List all contexts
kubectl config get-contexts

# Switch context
kubectl config use-context <context-name>

# Cluster info
kubectl cluster-info
```

### Namespaces

```bash
# List all namespaces
kubectl get namespaces

# List all resources in a namespace
kubectl get all -n com-clean-dev

# Delete namespace (and everything in it)
kubectl delete namespace com-clean-dev
```

### Pods

```bash
# List pods
kubectl get pods -n com-clean-dev

# List pods with node info
kubectl get pods -n com-clean-dev -o wide

# Watch pods in real-time
kubectl get pods -n com-clean-dev -w

# Describe a pod (events, conditions, volumes)
kubectl describe pod <pod-name> -n com-clean-dev

# Get pod logs
kubectl logs <pod-name> -n com-clean-dev

# Follow logs
kubectl logs -f <pod-name> -n com-clean-dev

# Logs from a specific container
kubectl logs <pod-name> -c <container-name> -n com-clean-dev

# Previous container logs (after crash)
kubectl logs <pod-name> --previous -n com-clean-dev
```

### Exec into a Pod

```bash
# Open a shell
kubectl exec -it <pod-name> -n com-clean-dev -- /bin/bash

# Run a single command
kubectl exec <pod-name> -n com-clean-dev -- env
```

### Deployments & StatefulSets

```bash
# List deployments
kubectl get deployments -n com-clean-dev

# List statefulsets
kubectl get statefulsets -n com-clean-dev

# Rollout status
kubectl rollout status deployment/redis -n com-clean-dev
kubectl rollout status statefulset/mariadb -n com-clean-dev
kubectl rollout status statefulset/infinispan -n com-clean-dev

# Rollout history
kubectl rollout history statefulset/infinispan -n com-clean-dev

# Restart a deployment (rolling restart)
kubectl rollout restart deployment/redis -n com-clean-dev

# Scale a statefulset
kubectl scale statefulset/infinispan --replicas=3 -n com-clean-dev
```

### Services

```bash
# List services
kubectl get services -n com-clean-dev

# Get NodePort info
kubectl get service infinispan -n com-clean-dev -o jsonpath='{.spec.ports[0].nodePort}'

# Port-forward to local machine
kubectl port-forward svc/infinispan 11222:11222 -n com-clean-dev
kubectl port-forward svc/mariadb 3306:3306 -n com-clean-dev
kubectl port-forward svc/redis 6379:6379 -n com-clean-dev
```

### Secrets & ConfigMaps

```bash
# List secrets
kubectl get secrets -n com-clean-dev

# Decode a secret value
kubectl get secret mariadb-secret -n com-clean-dev \
  -o jsonpath='{.data.MARIADB_PASSWORD}' | base64 -d

# View configmap
kubectl get configmap mariadb-config -n com-clean-dev -o yaml
```

### Jobs

```bash
# List jobs
kubectl get jobs -n com-clean-dev

# Wait for job completion
kubectl wait --for=condition=complete --timeout=300s job/infinispan-cache-create -n com-clean-dev

# Job logs
kubectl logs job/infinispan-cache-create -n com-clean-dev

# Delete a completed job (to re-run it)
kubectl delete job infinispan-cache-create -n com-clean-dev
```

### PersistentVolumeClaims

```bash
# List PVCs
kubectl get pvc -n com-clean-dev

# Describe a PVC
kubectl describe pvc mariadb-data-mariadb-0 -n com-clean-dev
```

### Troubleshooting

```bash
# Events in a namespace (ordered by time)
kubectl get events -n com-clean-dev --sort-by='.lastTimestamp'

# Describe node (check resource pressure)
kubectl describe node <node-name>

# Resource usage (requires metrics-server)
kubectl top pods -n com-clean-dev
kubectl top nodes

# Check if a pod is ready
kubectl get pod <pod-name> -n com-clean-dev \
  -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}'
```

---

## Infinispan Local Dev Connectivity (Mac)

After deploying to a local cluster with NodePort:

```bash
# Test connectivity to Infinispan NodePort
nc -zv <node-ip> 31767

# Spring Boot application.properties
infinispan.remote.server-list=<node-ip>:31767
infinispan.remote.auth-username=infinispan
infinispan.remote.auth-password=abc@123
```

---

## GitHub Actions

Automated deployment workflow at `.github/workflows/k8s-deploy.yml`.

**Inputs** (workflow_dispatch):
- `environment` — dev | sit | prod
- `component` — all | db | redis | infinispan
- `db_service_mode` — override DB service type (optional)

**Required secret**: `KUBE_CONFIG_DATA` (base64-encoded kubeconfig)
