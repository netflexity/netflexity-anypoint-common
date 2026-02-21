package com.netflexity.anypoint.common.monitor;

import com.netflexity.anypoint.common.model.QueueStats;

import java.util.Map;

/**
 * Interface for providing queue metrics to the monitor scheduler.
 * 
 * Implementations should return the current queue statistics
 * collected by the specific exporter (MQ, Metrics, etc.).
 *
 * @author Netflexity
 * @version 1.0.0
 */
public interface MetricsProvider {

    /**
     * Get current queue statistics keyed by a unique identifier.
     *
     * @return map of queue key to QueueStats
     */
    Map<String, QueueStats> getCurrentQueueStats();
}
