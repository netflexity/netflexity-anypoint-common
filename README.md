<p align="center">
  <h1 align="center">Anypoint Common Library</h1>
  <p align="center">
    Shared Java library for Netflexity Anypoint Platform monitoring exporters - config, auth, discovery, monitoring, and notifications.
  </p>
</p>

<p align="center">
  <a href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html"><img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white" alt="Java 17"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.4"></a>
  <a href="https://micrometer.io/"><img src="https://img.shields.io/badge/Micrometer-Prometheus-E6522C?logo=prometheus&logoColor=white" alt="Micrometer"></a>
</p>

---

## Overview

`anypoint-common` is the shared foundation library used by all Netflexity Anypoint Platform exporters:

- [**Anypoint MQ Prometheus Exporter**](https://bitbucket.org/netflexity/anypoint-mq-prometheus-exporter) - MQ queue and exchange metrics
- [**Anypoint Metrics Prometheus Exporter**](https://bitbucket.org/netflexity/anypoint-metrics-prometheus-exporter) - Full platform metrics (MTK port)

Instead of duplicating authentication, discovery, monitoring, and notification code across exporters, this library provides a single, well-tested implementation that all exporters depend on.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   anypoint-common                       │
│                                                         │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────────┐  │
│  │  client   │  │  config   │  │     discovery        │  │
│  │ AuthClient│  │ Anypoint  │  │ EnvironmentDiscovery │  │
│  │ (OAuth2)  │  │ Exporter  │  │ (orgs, envs, auto)   │  │
│  └──────────┘  │ Monitor   │  └──────────────────────┘  │
│                │ Notif.    │                             │
│  ┌──────────┐  └───────────┘  ┌──────────────────────┐  │
│  │  health   │                │      monitor          │  │
│  │ Actuator  │  ┌───────────┐ │ Evaluator, Scheduler │  │
│  │ indicator │  │  license  │ │ Definitions, State   │  │
│  └──────────┘  │ FREE/PRO  │ │ MetricsProvider (SPI)│  │
│                └───────────┘ └──────────────────────┘  │
│  ┌──────────┐                                           │
│  │   web     │  ┌──────────────────────────────────────┐│
│  │ REST API  │  │         notification                 ││
│  │ /api/*    │  │ Slack, PagerDuty, Email, Teams,      ││
│  └──────────┘  │ Webhook + NotificationDispatcher      ││
│                └──────────────────────────────────────┘│
│  ┌──────────┐                                           │
│  │  model    │  AuthToken, Queue, QueueStats            │
│  └──────────┘                                           │
└─────────────────────────────────────────────────────────┘
         ▲                           ▲
         │                           │
    ┌────┴────┐               ┌──────┴──────┐
    │ MQ      │               │  Metrics    │
    │ Exporter│               │  Exporter   │
    └─────────┘               └─────────────┘
```

## Package Structure

| Package | Description |
|---------|-------------|
| `client` | `AnypointAuthClient` - OAuth2 token management (username/password + Connected App) with caching and auto-refresh |
| `config` | `AnypointConfig` (connection, auth, scrape settings), `ExporterConfig` (WebClient, metrics beans), `MonitorConfig`, `NotificationChannelConfiguration` |
| `discovery` | `EnvironmentDiscoveryService` - auto-discovers orgs and environments from Anypoint Platform on startup and periodically |
| `health` | `AnypointHealthIndicator` - Spring Boot Actuator health check for Anypoint connectivity (cached, 30s TTL) |
| `license` | `LicenseService` - feature gating for FREE/PRO tiers (monitors, notifications, health scores) |
| `model` | `AuthToken`, `Queue`, `QueueStats` - shared data models with JSON deserialization |
| `monitor` | `MonitorDefinition`, `MonitorEvaluator`, `MonitorScheduler`, `MonitorState`, `MonitorResult`, `MetricsProvider` interface |
| `notification` | `NotificationChannel` interface + 5 implementations: Slack, PagerDuty, Email, Microsoft Teams, Webhook |
| `web` | `DiscoveryController` (`/api/status`, `/api/discover`), `MonitorController` (`/api/monitors`, `/api/health-scores`, `/api/license`) |

## Maven Coordinates

```xml
<dependency>
    <groupId>com.netflexity.anypoint</groupId>
    <artifactId>anypoint-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

Add the dependency to your exporter's `pom.xml`. The library uses Spring Boot auto-configuration - beans are created automatically when the right properties are set.

### Required Properties

```yaml
anypoint:
  base-url: https://anypoint.mulesoft.com
  auth:
    client-id: ${ANYPOINT_CLIENT_ID}
    client-secret: ${ANYPOINT_CLIENT_SECRET}
  auto-discovery: true
```

### Monitor Integration

Exporters implement `MetricsProvider` to plug into the shared monitoring framework:

```java
@Component
public class MyMetricsCollector implements MetricsProvider {
    @Override
    public Map<String, QueueStats> getCurrentQueueStats() {
        // Return your collected stats - the monitor scheduler
        // evaluates them against configured monitor definitions
    }
}
```

### Monitor Types

The evaluation engine supports these monitor types out of the box:

| Type | Description |
|------|-------------|
| `QUEUE_DEPTH` | Alert when queue depth exceeds threshold |
| `DLQ_ALERT` | Alert when dead letter queues have messages |
| `THROUGHPUT_DROP` | Detect sudden decreases in throughput |
| `THROUGHPUT_SPIKE` | Detect unusual spikes in throughput |
| `QUEUE_HEALTH` | Composite health score (0-100) per queue |

### Notification Channels

All channels are configured via `application.yml` and auto-wired when `anypoint.monitors.enabled=true`:

| Channel | Configuration |
|---------|--------------|
| **Slack** | Incoming webhook URL |
| **PagerDuty** | Events API v2 routing key with severity mapping |
| **Email** | Spring Mail (SMTP) with formatted subjects |
| **Microsoft Teams** | Incoming webhook with adaptive cards |
| **Webhook** | Generic HTTP POST with custom headers and bearer auth |

## Requirements

- Java 17+
- Spring Boot 3.4.x

## Building

```bash
mvn clean install
```

This installs the library to your local Maven repository for use by the exporter projects.

---

<p align="center">
  Built by <a href="https://netflexity.com">Netflexity</a>
</p>
