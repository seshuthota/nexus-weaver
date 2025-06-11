# Nexus Weaver Implementation Plan - Complete Beginner Guide

## Overview
This guide assumes you've **never used Kubernetes, Docker, or any DevOps tools**. We'll build the financial market data platform step-by-step with detailed explanations of every command and concept.

## What We're Building (Simple Terms)
- **Kubernetes**: Think of it as a "smart server manager" that runs your applications
- **Docker**: Packages your application into a "container" (like a shipping container for code)
- **Apache Camel**: A tool that connects different systems together (like a universal translator)
- **RabbitMQ**: A message delivery system (like a super-fast postal service for your applications)

---

## 📊 Progress Tracker

### ✅ **Phase 1: Your First Kubernetes Cluster (COMPLETED)**
**Date Completed:** January 15, 2024

**What Was Accomplished:**
- ✅ **System Setup**: Updated Ubuntu system and installed all prerequisites
- ✅ **Docker Installation**: Successfully installed Docker CE and verified with hello-world
- ✅ **kubectl Installation**: Installed Kubernetes command-line tool
- ✅ **k3s Cluster**: Created single-node lightweight Kubernetes cluster
- ✅ **Resource Limits**: Configured namespace quotas and limits (2Gi memory, 4 CPU cores max)
- ✅ **First Deployment**: Successfully deployed 2-replica nginx application
- ✅ **Application Test**: Verified app accessibility via port-forward to localhost:8080
- ✅ **Resource Monitoring**: Confirmed resource usage within limits (128Mi memory used)

**Key Metrics Achieved:**
- **Memory Usage**: ~128Mi for application + ~400Mi for k3s system = ~528Mi total
- **Pod Status**: 2/2 pods running successfully
- **Resource Quota**: 64Mi/2Gi memory requests, 50m/2 CPU requests used
- **Application Response**: nginx welcome page accessible via browser

**Files Created:**
- `~/nexus-weaver/resource-quota.yaml` - Resource limits configuration
- `~/nexus-weaver/hello-app.yaml` - First application deployment

**Next Phase:** Phase 2 - GitOps with ArgoCD

### ✅ **Phase 2: GitOps with ArgoCD (COMPLETED)**
**Date Completed:** January 15, 2024

**What Was Accomplished:**
- ✅ **ArgoCD Installation**: Successfully installed ArgoCD in k3s cluster with all components running
- ✅ **GitOps Repository Structure**: Created organized Git repository with kustomize base/overlay pattern
- ✅ **GitHub Integration**: Connected to https://github.com/seshuthota/nexus-weaver.git with proper Git workflow
- ✅ **ArgoCD Application**: Deployed and configured hello-nexus-dev application managed by ArgoCD
- ✅ **GitOps Workflow**: Demonstrated complete Git → ArgoCD → Kubernetes automation
- ✅ **Environment Management**: Dev overlay successfully reduces replicas and adds environment labels
- ✅ **Auto-Sync**: Verified ArgoCD automatically detects Git changes and deploys to cluster
- ✅ **Self-Healing**: ArgoCD maintains desired state with automated sync and prune capabilities

**Key Metrics Achieved:**
- **ArgoCD Status**: All 7 ArgoCD pods running successfully
- **Application Status**: hello-nexus-dev shows "Synced" and "Healthy"
- **GitOps Demo**: Successfully scaled deployment from 1 to 2 replicas via Git commit
- **Resource Efficiency**: Dev overlay uses 1-2 replicas vs base 2 replicas
- **Automation**: Zero-touch deployment after Git push

**Files Created:**
- `gitops/base/hello-app/` - Base Kubernetes manifests with kustomization
- `gitops/overlays/dev/` - Dev environment overlay with replica patches
- `gitops/applications/hello-nexus-dev.yaml` - ArgoCD application definition
- Connected to GitHub repository with proper branching strategy

**ArgoCD Access:**
- Web UI: https://localhost:8080
- Username: admin
- Password: zd0fy6KN2nmx9hKT

**Next Phase:** Phase 3 - Apache Camel Services

### 🔄 **Phase 3: Simple Apache Camel Service (IN PROGRESS)**
**Target Completion:** TBD

### ⏳ **Phase 4: Lightweight Message Queue (PLANNED)**
**Target Completion:** TBD

### ⏳ **Phase 5: Monitoring & Observability (PLANNED)**
**Target Completion:** TBD

---

## Prerequisites & Setup (Ubuntu PC)

### System Requirements
- **RAM**: 4GB minimum (8GB recommended)
- **CPU**: 2 cores minimum
- **Disk**: 20GB free space
- **OS**: Ubuntu 20.04+ (works on most Linux distros)

### What You Need to Install

**1. Update Your System**
```bash
# Always start with system updates
sudo apt update && sudo apt upgrade -y

# Install basic tools we'll need
sudo apt install -y curl wget git vim nano htop
```

**2. Install Docker (Container Runtime)**
```bash
# Docker is what actually runs our containers
# Remove any old Docker versions first
sudo apt remove docker docker-engine docker.io containerd runc

# Install Docker from official repository
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# Add your user to docker group (so you don't need sudo)
sudo usermod -aG docker $USER

# IMPORTANT: Log out and log back in for group changes to take effect
# Or run: newgrp docker

# Test Docker installation
docker --version
docker run hello-world
```

**3. Install kubectl (Kubernetes Command Line Tool)**
```bash
# This is how you "talk" to Kubernetes clusters
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Make it executable and move to system path
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify installation
kubectl version --client
```

**4. Install k3s (Lightweight Kubernetes)**
```bash
# k3s is perfect for local development - uses much less RAM than full Kubernetes
# This single command installs everything
curl -sfL https://get.k3s.io | sh -

# k3s automatically starts, but let's verify
sudo systemctl status k3s

# Set up kubectl to work with k3s
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config

# Test your cluster
kubectl get nodes
# You should see something like:
# NAME        STATUS   ROLES                  AGE   VERSION
# your-pc     Ready    control-plane,master   1m    v1.28.2+k3s1
```

**5. Install Helm (Kubernetes Package Manager)**
```bash
# Helm installs complex applications with a single command
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verify installation
helm version
```

**6. Install Java and Maven (for building our applications)**
```bash
# We'll build Java applications with Apache Camel
sudo apt install -y openjdk-17-jdk maven

# Verify installations
java -version
mvn -version
```

---

## Phase 1: Your First Kubernetes Cluster (Week 1)

### Step 1.1: Understanding What We're Creating

**What is a Kubernetes Cluster?**
- A **cluster** = A group of computers (servers) working together
- **Nodes** = Individual computers in the cluster (in our case, just your PC)
- **Pods** = Where your applications actually run (like apartments in a building)
- **Services** = How pods talk to each other (like a phone directory)

### Step 1.2: Verify Your k3s Cluster

**Your cluster is already running!** When you installed k3s, it automatically created a single-node cluster on your PC.

```bash
# Check your cluster status
kubectl get nodes

# You should see output like:
# NAME        STATUS   ROLES                  AGE   VERSION
# your-pc     Ready    control-plane,master   5m    v1.28.2+k3s1

# Check what's running in your cluster
kubectl get pods --all-namespaces

# You'll see system pods like:
# NAMESPACE     NAME                              READY   STATUS    RESTARTS   AGE
# kube-system   local-path-provisioner-xxx        1/1     Running   0          5m
# kube-system   coredns-xxx                       1/1     Running   0          5m
# kube-system   metrics-server-xxx                1/1     Running   0          5m
```

### Step 1.3: Understanding Resource Limits (Important for Low RAM!)

**Configure resource limits to prevent your PC from freezing:**
```bash
# Create a resource quota for our namespace
mkdir -p ~/nexus-weaver
cd ~/nexus-weaver

# Create resource limits file
cat << EOF > resource-quota.yaml
# This prevents any single application from using too much RAM/CPU
apiVersion: v1
kind: ResourceQuota
metadata:
  name: nexus-weaver-quota
  namespace: nexus-weaver
spec:
  hard:
    requests.cpu: "2"           # Total CPU requests for all pods
    requests.memory: 2Gi        # Total memory requests for all pods  
    limits.cpu: "4"             # Total CPU limits for all pods
    limits.memory: 4Gi          # Total memory limits for all pods
    pods: "20"                  # Maximum number of pods

---
# This sets default limits for pods that don't specify them
apiVersion: v1
kind: LimitRange
metadata:
  name: nexus-weaver-limits
  namespace: nexus-weaver
spec:
  limits:
  - default:                    # Default limits if not specified
      cpu: "200m"               # 0.2 CPU cores
      memory: "256Mi"           # 256 megabytes
    defaultRequest:             # Default requests if not specified
      cpu: "100m"               # 0.1 CPU cores  
      memory: "128Mi"           # 128 megabytes
    type: Container
EOF

# Create our namespace and apply limits
kubectl create namespace nexus-weaver
kubectl apply -f resource-quota.yaml

# Verify the limits are in place
kubectl describe namespace nexus-weaver
```

### Step 1.4: Monitor Your System Resources

**Install monitoring tools to watch resource usage:**
```bash
# Install htop for real-time monitoring
sudo apt install -y htop

# Monitor system resources while running Kubernetes
htop

# In another terminal, watch Kubernetes resource usage
watch kubectl top nodes

# If the above doesn't work, wait a few minutes for metrics-server to start
# Then try: kubectl top pods --all-namespaces
```

**System monitoring commands:**
```bash
# Check memory usage
free -h

# Check disk usage  
df -h

# Check running Docker containers
docker ps

# Check k3s status
sudo systemctl status k3s

# View k3s logs if something goes wrong
sudo journalctl -u k3s -f
```

### Step 1.5: Deploy Your First Resource-Efficient Application

**Create a lightweight test application:**
```bash
# Create a file called hello-app.yaml
cd ~/nexus-weaver
nano hello-app.yaml
```

**hello-app.yaml (resource-efficient version):**
```yaml
# This describes an application deployment
apiVersion: apps/v1
kind: Deployment                    # Type of Kubernetes object
metadata:
  name: hello-nexus                 # Name of your application
  namespace: nexus-weaver           # Put it in our namespace
  labels:
    app: hello-nexus
spec:
  replicas: 2                       # Only 2 copies (instead of 3) to save RAM
  selector:
    matchLabels:
      app: hello-nexus              # How to find the pods that belong to this deployment
  template:                         # Template for creating pods
    metadata:
      labels:
        app: hello-nexus
    spec:
      containers:                   # What to run inside each pod
      - name: hello-container
        image: nginx:alpine         # Alpine is much smaller than regular nginx
        ports:
        - containerPort: 80         # Application listens on port 80
        resources:                  # Resource limits (very important for low RAM!)
          requests:                 # What the app needs to start
            memory: "32Mi"          # Only 32 megabytes of RAM
            cpu: "25m"              # 25 millicores (0.025 CPU cores)
          limits:                   # Maximum the app can use
            memory: "64Mi"          # Maximum 64 megabytes of RAM
            cpu: "50m"              # Maximum 50 millicores (0.05 CPU cores)

---
# The --- separates multiple Kubernetes objects in one file

# This creates a Service (how other things talk to your app)
apiVersion: v1
kind: Service
metadata:
  name: hello-nexus-service
  namespace: nexus-weaver
spec:
  selector:
    app: hello-nexus                # Connect to pods with this label
  ports:
    - protocol: TCP
      port: 80                      # Port the service listens on
      targetPort: 80                # Port the pods listen on
  type: ClusterIP                   # Only accessible from inside the cluster
```

**Deploy your application:**
```bash
# Apply the configuration to your cluster
kubectl apply -f hello-app.yaml

# Check if it's running (in our namespace)
kubectl get deployments -n nexus-weaver
# Should show:
# NAME          READY   UP-TO-DATE   AVAILABLE   AGE
# hello-nexus   2/2     2            2           30s

# See the actual pods and their resource usage
kubectl get pods -n nexus-weaver
kubectl top pods -n nexus-weaver

# Check resource quota usage
kubectl describe resourcequota nexus-weaver-quota -n nexus-weaver
```

**Test your application:**
```bash
# Forward traffic from your PC to one of the pods
# This creates a tunnel: localhost:8080 → pod:80
kubectl port-forward service/hello-nexus-service 8080:80 -n nexus-weaver

# Now open your browser and go to: http://localhost:8080
# You should see the nginx welcome page!
# Press Ctrl+C to stop the port forwarding
```

**Watch resource usage while testing:**
```bash
# In another terminal, monitor system resources
htop

# Watch Docker container resource usage
docker stats

# Monitor Kubernetes pod resource usage
watch kubectl top pods -n nexus-weaver
```

### 1.2 GitOps Setup with ArgoCD

**Install ArgoCD:**
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Expose ArgoCD server
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'
```

**Create GitOps repository structure:**
```
nexus-weaver-infra/
├── applications/
├── base/
│   ├── rabbitmq/
│   ├── elasticsearch/
│   └── monitoring/
└── overlays/
    ├── dev/
    ├── staging/
    └── prod/
```

### 1.3 Sealed Secrets for Financial Credentials

```bash
# Install sealed-secrets controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# Create trading credentials secret
echo -n 'fix-gateway-password' | kubectl create secret generic trading-creds \
  --dry-run=client --from-file=password=/dev/stdin -o yaml | \
  kubeseal -o yaml > sealed-trading-creds.yaml
```

---

## Phase 2: Messaging Infrastructure (Weeks 3-4)

### 2.1 RabbitMQ Cluster with Streams

**RabbitMQ StatefulSet with Streams enabled:**
```yaml
# rabbitmq-cluster.yaml
apiVersion: rabbitmq.com/v1beta1
kind: RabbitmqCluster
metadata:
  name: nexus-rabbitmq
spec:
  replicas: 5
  rabbitmq:
    additionalConfig: |
      rabbitmq_stream.tcp_listen_options.nodelay = true
      rabbitmq_stream.tcp_listen_options.keepalive = true
      stream_max_segment_size_bytes = 500000000
  resources:
    requests:
      cpu: "2"
      memory: 8Gi
    limits:
      cpu: "4"
      memory: 16Gi
  persistence:
    storageClassName: fast-ssd
    storage: 100Gi
```

**Stream topology setup:**
```bash
# Create streams after cluster is ready
kubectl exec -it nexus-rabbitmq-server-0 -- rabbitmq-streams add_stream market-prices-stream \
  --max-length-bytes 1000000000

kubectl exec -it nexus-rabbitmq-server-0 -- rabbitmq-streams add_stream trade-executions-stream \
  --max-length-bytes 500000000
```

### 2.2 Stream Publisher Test Service

**Create simple publisher to validate streams:**
```java
@Service
public class StreamTestPublisher {
    
    @Autowired
    private RabbitStreamTemplate streamTemplate;
    
    @EventListener(ApplicationReadyEvent.class)
    public void startPublishing() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::publishTestData, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    private void publishTestData() {
        MarketDataEvent event = MarketDataEvent.builder()
            .symbol("AAPL")
            .price(BigDecimal.valueOf(150.25))
            .timestamp(Instant.now())
            .volume(1000L)
            .build();
            
        streamTemplate.convertAndSend("market-prices-stream", event);
    }
}
```

---

## Phase 3: Data Ingestion Services (Weeks 5-7)

### 3.1 Market Data Ingestion Service

**Spring Boot + Camel service structure:**
```
market-data-ingestion/
├── src/main/java/
│   ├── config/
│   │   ├── CamelConfig.java
│   │   └── RabbitConfig.java
│   ├── routes/
│   │   ├── FIXIngestionRoute.java
│   │   ├── ReutersIngestionRoute.java
│   │   └── WebSocketIngestionRoute.java
│   ├── processors/
│   │   ├── MessageValidator.java
│   │   └── DataNormalizer.java
│   └── MarketDataIngestionApplication.java
└── src/main/resources/
    ├── application.yml
    └── camel-context.xml
```

**FIX Protocol ingestion route:**
```java
@Component
public class FIXIngestionRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        // FIX Protocol endpoint
        from("mina:tcp://{{fix.gateway.host}}:{{fix.gateway.port}}?codec=#fixCodec")
            .routeId("fix-ingestion")
            .unmarshal().bindy(FixMessage.class)
            .process(exchange -> {
                FixMessage msg = exchange.getIn().getBody(FixMessage.class);
                log.info("Received FIX message: {}", msg.getMessageType());
                
                // Add routing metadata
                exchange.getIn().setHeader("MessageType", msg.getMessageType());
                exchange.getIn().setHeader("Symbol", msg.getSymbol());
                exchange.getIn().setHeader("Source", "FIX");
            })
            .choice()
                .when(header("MessageType").isEqualTo("D")) // New Order
                    .to("rabbitmq-stream:order-flow-stream")
                .when(header("MessageType").isEqualTo("8")) // Execution Report
                    .to("rabbitmq-stream:trade-executions-stream")
                .otherwise()
                    .log("Unknown FIX message type: ${header.MessageType}")
            .end();
    }
}
```

**WebSocket market data ingestion:**
```java
@Component
public class WebSocketIngestionRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        // Multiple exchange WebSocket feeds
        from("websocket:ws://{{nyse.feed.url}}/market-data")
            .routeId("nyse-websocket")
            .unmarshal().json(JsonLibrary.Jackson)
            .process(this::normalizeNYSEData)
            .setHeader("Source", constant("NYSE"))
            .to("rabbitmq-stream:market-prices-stream");
            
        from("websocket:ws://{{nasdaq.feed.url}}/market-data")
            .routeId("nasdaq-websocket")
            .unmarshal().json(JsonLibrary.Jackson)
            .process(this::normalizeNASDAQData)
            .setHeader("Source", constant("NASDAQ"))
            .to("rabbitmq-stream:market-prices-stream");
    }
    
    private void normalizeNYSEData(Exchange exchange) {
        // NYSE-specific normalization logic
        Map<String, Object> nyseData = exchange.getIn().getBody(Map.class);
        
        MarketDataEvent event = MarketDataEvent.builder()
            .symbol((String) nyseData.get("s"))
            .price(new BigDecimal(nyseData.get("p").toString()))
            .volume(Long.valueOf(nyseData.get("v").toString()))
            .timestamp(Instant.now())
            .exchange("NYSE")
            .build();
            
        exchange.getIn().setBody(event);
    }
}
```

### 3.2 Data Transformation Service

**Complex transformation pipeline:**
```java
@Component  
public class DataTransformationRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        from("rabbitmq-stream:market-prices-stream?offset=latest")
            .routeId("data-transformation")
            .threads(20, 100)
            .choice()
                .when(header("Source").isEqualTo("REUTERS"))
                    .to("direct:transform-reuters")
                .when(header("Source").isEqualTo("BLOOMBERG"))
                    .to("direct:transform-bloomberg")
                .when(header("Source").isEqualTo("NYSE"))
                    .to("direct:transform-nyse")
            .end()
            .marshal().json()
            .to("rabbitmq-stream:processed-market-data");
            
        // Reuters transformation sub-route
        from("direct:transform-reuters")
            .process(this::parseReutersFormat)
            .enrich("sql:SELECT sector, market_cap FROM company_metadata WHERE symbol = ?")
            .enrich("redis:company-cache?command=GET&key=${header.symbol}")
            .process(this::mergeMetadata)
            .to("direct:output");
    }
}
```

---

## Phase 4: Processing Services (Weeks 8-10)

### 4.1 Real-time Risk Service

**Risk calculation with streaming aggregation:**
```java
@Component
public class RiskCalculationRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        from("rabbitmq-stream:trade-executions-stream?offset=latest")
            .routeId("risk-calculation")
            .process(this::calculatePositionDelta)
            .aggregate(header("portfolioId"))
                .aggregationStrategy(new PortfolioAggregationStrategy())
                .completionTimeout(50) // 50ms risk calculation windows
                .completionSize(100)   // Or batch of 100 trades
            .process(exchange -> {
                Portfolio portfolio = exchange.getIn().getBody(Portfolio.class);
                
                // Calculate Value at Risk (VaR)
                BigDecimal var95 = riskCalculator.calculateVaR(portfolio, 0.95);
                BigDecimal exposureRatio = portfolio.getTotalExposure()
                    .divide(portfolio.getTotalCapital(), RoundingMode.HALF_UP);
                
                RiskMetrics risk = RiskMetrics.builder()
                    .portfolioId(portfolio.getId())
                    .var95(var95)
                    .exposureRatio(exposureRatio)
                    .timestamp(Instant.now())
                    .build();
                    
                exchange.getIn().setBody(risk);
                exchange.getIn().setHeader("riskLevel", var95);
            })
            .choice()
                .when(simple("${body.exposureRatio} > 0.95"))
                    .multicast()
                        .to("rabbitmq:risk-alerts-queue")
                        .to("elasticsearch:risk-violations?operation=index")
                    .end()
                .otherwise()
                    .to("rabbitmq:risk-updates-queue")
            .end();
    }
}
```

### 4.2 Fraud Detection Service

**Complex Event Processing for suspicious patterns:**
```java
@Component
public class FraudDetectionRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        from("rabbitmq-stream:trade-executions-stream")
            .routeId("fraud-detection")
            .idempotentConsumer(header("tradeId"))
                .messageIdRepository("#redisIdempotentRepo")
            .to("seda:fraud-analysis?waitForTaskToComplete=Never");
            
        from("seda:fraud-analysis?concurrentConsumers=10")
            .process(this::extractTraderProfile)
            .enrich("sql:SELECT avg_daily_volume, typical_instruments FROM trader_profiles WHERE trader_id = ?")
            .choice()
                .when(method("fraudDetector", "isVolumeAnomaly"))
                    .setHeader("AlertType", constant("VOLUME_ANOMALY"))
                    .to("direct:generate-alert")
                .when(method("fraudDetector", "isTimingAnomaly"))
                    .setHeader("AlertType", constant("TIMING_ANOMALY"))
                    .to("direct:generate-alert")
                .when(method("fraudDetector", "isPatternAnomaly"))
                    .setHeader("AlertType", constant("PATTERN_ANOMALY"))  
                    .to("direct:generate-alert")
            .end();
            
        from("direct:generate-alert")
            .process(this::enrichAlertData)
            .multicast()
                .to("rabbitmq:compliance-alerts-queue")
                .to("elasticsearch:fraud-alerts?operation=index")
                .to("websocket:compliance-dashboard")
            .end();
    }
}
```

---

## Phase 5: Search & Analytics (Weeks 11-12)

### 5.1 Elasticsearch Deployment

**ECK Operator installation:**
```bash
kubectl create -f https://download.elastic.co/downloads/eck/2.10.0/crds.yaml
kubectl apply -f https://download.elastic.co/downloads/eck/2.10.0/operator.yaml
```

**Multi-tier Elasticsearch cluster:**
```yaml
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: nexus-elasticsearch
spec:
  version: 8.11.0
  nodeSets:
  # Hot tier - current day data
  - name: hot
    count: 6
    config:
      node.roles: ["data_hot", "data_content"]
      xpack.ml.enabled: true
    podTemplate:
      spec:
        containers:
        - name: elasticsearch
          resources:
            requests:
              memory: 32Gi
              cpu: 8
            limits:
              memory: 32Gi
              cpu: 8
        nodeSelector:
          workload: storage-optimized
    volumeClaimTemplates:
    - metadata:
        name: elasticsearch-data
      spec:
        accessModes:
        - ReadWriteOnce
        resources:
          requests:
            storage: 1Ti
        storageClassName: fast-nvme
        
  # Warm tier - 30 days retention
  - name: warm
    count: 4
    config:
      node.roles: ["data_warm"]
    podTemplate:
      spec:
        containers:
        - name: elasticsearch
          resources:
            requests:
              memory: 16Gi
              cpu: 4
    volumeClaimTemplates:
    - metadata:
        name: elasticsearch-data
      spec:
        accessModes:
        - ReadWriteOnce
        resources:
          requests:
            storage: 2Ti
        storageClassName: fast-ssd
```

### 5.2 Index Templates and Policies

**Market data index template:**
```json
{
  "index_patterns": ["market-data-*"],
  "template": {
    "settings": {
      "number_of_shards": 10,
      "number_of_replicas": 1,
      "index.lifecycle.name": "market-data-policy",
      "index.codec": "best_compression"
    },
    "mappings": {
      "properties": {
        "symbol": {"type": "keyword"},
        "timestamp": {"type": "date", "format": "epoch_millis"},
        "price": {"type": "scaled_float", "scaling_factor": 10000},
        "volume": {"type": "long"},
        "source": {"type": "keyword"},
        "exchange": {"type": "keyword"},
        "trader_id": {"type": "keyword", "index": false},
        "portfolio_id": {"type": "keyword"},
        "risk_score": {"type": "float"}
      }
    }
  }
}
```

**ILM Policy for cost optimization:**
```json
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_size": "10GB",
            "max_age": "1d"
          }
        }
      },
      "warm": {
        "min_age": "1d",
        "actions": {
          "allocate": {
            "require": {
              "data": "warm"
            }
          }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "allocate": {
            "require": {
              "data": "cold"
            }
          }
        }
      },
      "delete": {
        "min_age": "2y"
      }
    }
  }
}
```

---

## Phase 6: Observability & Monitoring (Weeks 13-14)

### 6.1 Prometheus Stack Deployment

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace \
  --set grafana.enabled=true \
  --set prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.resources.requests.storage=100Gi
```

### 6.2 Custom Business Metrics

**Spring Boot Actuator metrics:**
```java
@Component
public class BusinessMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter marketDataCounter;
    private final Timer orderProcessingTimer;
    private final Gauge activeSessionsGauge;
    
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.marketDataCounter = Counter.builder("market.data.processed.total")
            .description("Total market data messages processed")
            .tag("source", "unknown")
            .register(meterRegistry);
            
        this.orderProcessingTimer = Timer.builder("order.processing.latency")
            .description("Order processing latency")
            .register(meterRegistry);
            
        this.activeSessionsGauge = Gauge.builder("active.trading.sessions")
            .description("Number of active trading sessions")
            .register(meterRegistry, this, BusinessMetrics::getActiveSessionCount);
    }
    
    public void recordMarketData(String source, String symbol) {
        marketDataCounter.increment(
            Tags.of(
                Tag.of("source", source),
                Tag.of("symbol", symbol)
            )
        );
    }
    
    public Timer.Sample startOrderTimer() {
        return Timer.start(meterRegistry);
    }
}
```

### 6.3 Critical Alerting Rules

```yaml
# prometheus-alerts.yaml
groups:
- name: nexus-weaver-alerts
  rules:
  # Business critical alerts
  - alert: HighOrderProcessingLatency
    expr: histogram_quantile(0.95, rate(order_processing_latency_seconds_bucket[5m])) > 0.001
    for: 30s
    labels:
      severity: critical
    annotations:
      summary: "Order processing latency exceeds 1ms"
      
  - alert: MarketDataStreamLag
    expr: (time() - market_data_last_received_timestamp) > 5
    for: 10s
    labels:
      severity: critical
    annotations:
      summary: "Market data stream is lagging"
      
  - alert: RiskLimitBreach
    expr: max(portfolio_risk_exposure_ratio) > 0.95
    for: 0s
    labels:
      severity: critical
    annotations:
      summary: "Portfolio risk exposure exceeds 95%"
      
  # Infrastructure alerts  
  - alert: RabbitMQHighQueueDepth
    expr: rabbitmq_queue_messages{queue=~".*-stream"} > 10000
    for: 5m
    labels:
      severity: warning
      
  - alert: ElasticsearchClusterHealth
    expr: elasticsearch_cluster_health_status != 0
    for: 1m
    labels:
      severity: critical
```

---

## Phase 7: CI/CD Pipeline (Weeks 15-16)

### 7.1 GitHub Actions Workflow

```yaml
# .github/workflows/deploy.yml
name: Deploy Nexus Weaver
on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '17'
        
    - name: Run Unit Tests
      run: ./mvnw test
      
    - name: Run Integration Tests
      run: ./mvnw verify -Pintegration-tests
      
    - name: Security Scan
      uses: github/super-linter/slim@v5
      
    - name: Performance Benchmarks
      run: |
        ./mvnw gatling:test
        # Fail if latency > 1ms
        if grep "mean.*[0-9][0-9][0-9][0-9]ms" target/gatling/results/*/simulation.log; then
          echo "Performance regression detected"
          exit 1
        fi

  build-and-deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v4
    
    - name: Build Docker Images
      run: |
        docker build -t nexus-weaver/market-data-ingestion:${{ github.sha }} ./market-data-ingestion
        docker build -t nexus-weaver/risk-service:${{ github.sha }} ./risk-service
        
    - name: Push to Registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push nexus-weaver/market-data-ingestion:${{ github.sha }}
        
    - name: Update Kubernetes Manifests
      run: |
        yq eval '.spec.template.spec.containers[0].image = "nexus-weaver/market-data-ingestion:${{ github.sha }}"' \
          -i k8s/market-data-ingestion-deployment.yaml
          
    - name: Deploy via ArgoCD
      run: |
        git config --global user.email "ci@nexusweaver.com"
        git config --global user.name "CI Bot"
        git add k8s/
        git commit -m "Deploy ${{ github.sha }}"
        git push origin main
```

### 7.2 Specialized Financial Testing

```java
@SpringBootTest
@TestPropertySource(properties = {
    "camel.component.rabbitmq.hostname=localhost",
    "spring.elasticsearch.uris=http://localhost:9200"
})
class FinancialComplianceTests {
    
    @Test
    void testLatencyRequirements() {
        // Given market data event
        MarketDataEvent event = createSampleEvent();
        
        // When processing through the pipeline
        long startTime = System.nanoTime();
        processMarketData(event);
        long processingTime = System.nanoTime() - startTime;
        
        // Then processing must complete within 1ms
        assertThat(Duration.ofNanos(processingTime))
            .isLessThan(Duration.ofMillis(1));
    }
    
    @Test
    void testDataIntegrityAndAuditTrail() {
        // Given a trade execution
        TradeExecution trade = createSampleTrade();
        
        // When processed through fraud detection
        processTradeExecution(trade);
        
        // Then audit trail must be complete
        AuditTrail auditTrail = auditService.getTradeAuditTrail(trade.getId());
        assertThat(auditTrail.getEntries()).hasSize(4); // Ingestion, validation, risk calc, storage
        assertThat(auditTrail.isComplete()).isTrue();
    }
    
    @Test
    void testRegulatoryCompliance() {
        // Test MiFID II transaction reporting requirements
        // Test SOX data retention requirements
        // Test real-time risk monitoring requirements
    }
}
```

---

## Phase 8: Production Readiness (Weeks 17-18)

### 8.1 Load Testing with Realistic Data

```java
// Gatling performance test
class MarketDataLoadTest extends Simulation {
    
    val marketDataScenario = scenario("Market Data Processing")
        .feed(csv("market-data-feed.csv").circular)
        .exec(
            ws("Connect to Market Data Stream")
                .connect("ws://localhost:8080/market-data-stream")
        )
        .pause(1.millisecond, 5.milliseconds)
        .repeat(1000000) {
            exec(
                ws("Send Market Data")
                    .sendText("""{"symbol":"${symbol}","price":${price},"volume":${volume}}""")
            )
            .pause(1.millisecond)
        };
        
    setUp(
        marketDataScenario.inject(
            constantUsersPerSec(50000).during(10.minutes) // 50k msgs/sec for 10 minutes
        )
    ).protocols(
        ws.baseUrl("ws://nexus-weaver-gateway")
    ).assertions(
        global.responseTime.percentile3.lt(1), // 99.9th percentile < 1ms
        global.failedRequests.percent.lt(0.01)  // < 0.01% failures
    );
}
```

### 8.2 Disaster Recovery Setup

```yaml
# Multi-region deployment with disaster recovery
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: nexus-weaver-multi-region
spec:
  generators:
  - clusters:
      selector:
        matchLabels:
          environment: production
  template:
    metadata:
      name: 'nexus-weaver-{{name}}'
    spec:
      project: default
      source:
        repoURL: https://github.com/nexus-weaver/infra
        targetRevision: HEAD
        path: overlays/{{name}}
      destination:
        server: '{{server}}'
        namespace: nexus-weaver
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
```

### 8.3 Production Monitoring Dashboard

```json
{
  "dashboard": {
    "title": "Nexus Weaver - Production Dashboard",
    "panels": [
      {
        "title": "Market Data Throughput",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(market_data_processed_total[5m])",
            "legendFormat": "{{source}} - {{symbol}}"
          }
        ]
      },
      {
        "title": "Order Processing Latency",
        "type": "graph", 
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(order_processing_latency_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          },
          {
            "expr": "histogram_quantile(0.99, rate(order_processing_latency_seconds_bucket[5m]))",
            "legendFormat": "99th percentile"
          }
        ]
      },
      {
        "title": "Risk Exposure by Portfolio",
        "type": "graph",
        "targets": [
          {
            "expr": "portfolio_risk_exposure_ratio",
            "legendFormat": "Portfolio {{portfolio_id}}"
          }
        ]
      }
    ]
  }
}
```

---

## Deployment Checklist

### Pre-Production
- [ ] All unit and integration tests passing
- [ ] Performance benchmarks meet sub-millisecond requirements  
- [ ] Security scans completed (container images, dependencies)
- [ ] Disaster recovery procedures tested
- [ ] Monitoring and alerting configured
- [ ] Documentation updated

### Production Deployment
- [ ] Blue-green deployment strategy implemented
- [ ] Database migrations completed successfully
- [ ] SSL certificates installed and validated
- [ ] Network policies applied and tested
- [ ] Backup procedures verified
- [ ] Incident response procedures documented

### Post-Deployment
- [ ] Smoke tests passing in production
- [ ] Business metrics flowing correctly
- [ ] Audit logs being generated and stored
- [ ] Compliance requirements verified
- [ ] Performance monitoring active
- [ ] Team trained on new system

---

## Success Metrics

**Technical KPIs:**
- Order processing latency < 1ms (99.9th percentile)
- Market data throughput > 50M messages/second
- System availability > 99.99%
- Zero data loss during normal operations

**Business KPIs:**
- Fraud detection accuracy > 95%
- Risk calculation accuracy > 99.9%
- Regulatory compliance score: 100%
- Client onboarding time < 24 hours

This implementation plan provides a structured approach to building the complete financial market data platform with all the advanced Kubernetes, Apache Camel, and streaming technologies you want to learn.
