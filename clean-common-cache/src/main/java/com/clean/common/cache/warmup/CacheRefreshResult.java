package com.clean.common.cache.warmup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Generic result DTO for cache refresh / warmup operations.
 * <p>
 * Provides provider-agnostic metrics that can be used for logging,
 * REST responses, and observability dashboards.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheRefreshResult {

    private String provider;
    private String cacheName;
    private int batchSize;
    private long totalRows;
    private long cachedRows;
    private long skippedRows;
    private long durationMs;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
