package com.clean.common.cache.warmup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * Template {@link ApplicationRunner} for cache warmup on application startup.
 * <p>
 * Subclasses provide the actual warmup logic via {@link #doWarmup()},
 * along with configuration flags for enabling warmup and fail-fast behavior.
 * </p>
 *
 * <p><b>Fail-fast mode:</b> When {@link #isFailFastEnabled()} returns {@code true},
 * warmup failures cause an {@link IllegalStateException} that prevents application startup.
 * When disabled, failures are logged and the application starts in degraded mode.</p>
 */
@Slf4j
public abstract class AbstractCacheWarmupRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        if (!isWarmupEnabled()) {
            log.info("Cache warmup disabled for cache={}", cacheName());
            return;
        }

        log.info("Cache warmup starting for cache={}", cacheName());
        try {
            CacheRefreshResult result = doWarmup();
            log.info("Cache warmup completed cache={} total={} cached={} skipped={} durationMs={}",
                    result.getCacheName(), result.getTotalRows(),
                    result.getCachedRows(), result.getSkippedRows(), result.getDurationMs());
        } catch (Exception ex) {
            if (isFailFastEnabled()) {
                throw new IllegalStateException("Cache warmup failed for cache=" + cacheName(), ex);
            }
            log.error("Cache warmup failed (degraded mode) cache={}", cacheName(), ex);
        }
    }

    /**
     * Performs the actual cache warmup logic (batch load, refresh, etc.).
     *
     * @return metrics describing the refresh result
     */
    protected abstract CacheRefreshResult doWarmup();

    /**
     * @return {@code true} if warmup should run on startup
     */
    protected abstract boolean isWarmupEnabled();

    /**
     * @return {@code true} if startup should fail when warmup encounters an error
     */
    protected abstract boolean isFailFastEnabled();

    /**
     * @return the cache name, used for logging
     */
    protected abstract String cacheName();
}
