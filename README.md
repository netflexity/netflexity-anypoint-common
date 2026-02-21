# Anypoint Common Library

Shared Java library for Netflexity Anypoint Platform exporters (MQ Prometheus Exporter, Metrics Exporter, etc.).

## What's Included

| Package | Description |
|---------|-------------|
| `config` | `AnypointConfig` (connection/auth settings), `ExporterConfig` (WebClient, metrics beans), `MonitorConfig`, `NotificationChannelConfiguration` |
| `client` | `AnypointAuthClient` — OAuth2 token management (username/password + Connected App) |
| `discovery` | `EnvironmentDiscoveryService` — auto-discovers orgs & environments from Anypoint Platform |
| `health` | `AnypointHealthIndicator` — Spring Boot Actuator health check for Anypoint connectivity |
| `web` | `DiscoveryController` (`/api/status`), `MonitorController` (`/api/monitors`, `/api/health-scores`) |
| `model` | `AuthToken`, `Queue`, `QueueStats` — shared data models |
| `monitor` | `MonitorDefinition`, `MonitorEvaluator`, `MonitorScheduler`, `MonitorState`, `MonitorResult`, `MetricsProvider` interface |
| `notification` | `NotificationChannel` interface + Slack, PagerDuty, Email, Teams, Webhook implementations |
| `license` | `LicenseService` — feature gating for PRO tier |
| `metrics` | (via `ExporterConfig.ExporterMetrics`) — scrape duration, error counters |

## Maven Coordinates

```xml
<dependency>
    <groupId>com.netflexity.anypoint</groupId>
    <artifactId>anypoint-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

Add the dependency to your exporter's `pom.xml`. The library uses Spring Boot auto-configuration — beans are created automatically when the right properties are set.

### Required Properties

```yaml
anypoint:
  base-url: https://anypoint.mulesoft.com
  auth:
    client-id: <your-connected-app-id>
    client-secret: <your-connected-app-secret>
  auto-discovery: true
```

### Monitor Integration

Exporters must implement `MetricsProvider` to plug into the monitor scheduler:

```java
@Component
public class MyMetricsCollector implements MetricsProvider {
    @Override
    public Map<String, QueueStats> getCurrentQueueStats() {
        // return your collected stats
    }
}
```

## Requirements

- Java 17+
- Spring Boot 3.4.x
