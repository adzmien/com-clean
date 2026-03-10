package com.clean.common.cache.policy;

/**
 * Strategy interface for determining whether a cache entry should be stored.
 * <p>
 * Domain modules implement this interface with their own entry types
 * to apply business-specific cache acceptance rules (e.g. filtering
 * sensitive entries, excluding incomplete records).
 * </p>
 *
 * @param <T> the cache entry type
 */
@FunctionalInterface
public interface CacheEligibilityPolicy<T> {

    /**
     * Evaluates whether the given entry should be cached.
     *
     * @param entry the candidate cache entry
     * @return {@code true} if the entry should be stored, {@code false} to skip
     */
    boolean shouldCache(T entry);
}
