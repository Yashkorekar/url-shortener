package com.yk.url_shortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-based Rate Limiter (Sliding Window Counter)
 *
 * How it works:
 * - For each incoming IP, we store a counter key in Redis: "rate_limit:<ip>"
 * - On first request: set counter = 1 with TTL = windowSeconds
 * - On subsequent requests within the window: increment counter
 * - If counter exceeds maxRequests: reject the request (429 Too Many Requests)
 * - After TTL expires: key is deleted automatically by Redis → window resets
 *
 * Example:
 *   IP: 192.168.1.1, maxRequests=20, windowSeconds=60
 *   Request 1..20  → allowed  (counter 1..20, TTL=60s)
 *   Request 21     → BLOCKED  (counter=21 > 20)
 *   After 60s      → Redis deletes key → window resets
 *
 * Why Redis for rate limiting?
 * - Atomic increment (INCR) is thread-safe, no race conditions
 * - TTL-based auto-expiry means no cleanup needed
 * - Works across multiple app instances (unlike in-memory maps)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.rate-limit.max-requests:20}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private long windowSeconds;

    private static final String KEY_PREFIX = "rate_limit:";

    /**
     * Check if the given IP is allowed to make a request.
     *
     * @param clientIp The client's IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String clientIp) {
        String key = KEY_PREFIX + clientIp;
        try {
            Long count = redisTemplate.opsForValue().increment(key);

            if (count == null) {
                // Redis returned null - fail open (allow request)
                log.warn("Redis returned null for key: {}. Allowing request.", key);
                return true;
            }

            // First request in this window → set the expiry
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }

            if (count > maxRequests) {
                log.warn("Rate limit exceeded for IP: {} (count={}, max={})", clientIp, count, maxRequests);
                return false;
            }

            return true;

        } catch (Exception e) {
            // If Redis is down, fail open → don't block traffic
            log.error("Redis error during rate limit check for IP: {}. Failing open.", clientIp, e);
            return true;
        }
    }

    /**
     * Get remaining requests in the current window for an IP.
     * Useful for returning rate limit headers.
     *
     * @param clientIp The client's IP address
     * @return remaining allowed requests (0 if exceeded)
     */
    public long getRemainingRequests(String clientIp) {
        String key = KEY_PREFIX + clientIp;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return maxRequests;
            long used = Long.parseLong(value.toString());
            return Math.max(0, maxRequests - used);
        } catch (Exception e) {
            return maxRequests;
        }
    }
}


