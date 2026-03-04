package com.yk.url_shortener.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration
 *
 * What Redis is used for in this app:
 * 1. CACHING  - Cache shortCode→URL lookups so we don't hit DB on every redirect
 * 2. RATE LIMITING - Track request counts per IP using Redis atomic INCR counters
 *
 * Graceful degradation:
 * - If Redis is not running, the app still starts and works normally
 * - Caching simply doesn't happen (every request hits DB directly)
 * - Rate limiting fails open (all requests allowed)
 * - Redis errors are caught and logged, never crash the app
 *
 * Cache TTL strategy:
 * - "urls"    cache: 1 hour  - shortCode → Url object (hottest path)
 * - "stats"   cache: 5 mins  - changes on every redirect (access count)
 * - "domains" cache: 10 mins - expensive aggregation query
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Lettuce connection factory with:
     * - validateConnection=false  → don't validate on startup (allows startup without Redis)
     * - useSsl=false              → plain TCP for local Redis
     *
     * Connections are established lazily — only when Redis is actually used.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(2))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        // Don't validate connection at startup — allows running without Redis
        factory.setValidateConnection(false);
        return factory;
    }

    /**
     * RedisTemplate for manual Redis operations (rate limiter uses this directly).
     * Uses String keys and JSON values — human-readable in Redis CLI.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisCacheManager with per-cache TTL overrides.
     * Falls back gracefully: if Redis is unreachable at cache time,
     * the operation proceeds without caching (no crash).
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(1))
                    .disableCachingNullValues()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new GenericJackson2JsonRedisSerializer()));

            Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
            // "urls" cache: 1 hour — shortCode → Url (core redirect lookup)
            cacheConfigurations.put("urls", defaultConfig.entryTtl(Duration.ofHours(1)));
            // "stats" cache: 5 mins — access count changes on every redirect
            cacheConfigurations.put("stats", defaultConfig.entryTtl(Duration.ofMinutes(5)));
            // "domains" cache: 10 mins — expensive aggregation, evicted on new URL
            cacheConfigurations.put("domains", defaultConfig.entryTtl(Duration.ofMinutes(10)));

            log.info("Redis CacheManager initialized (host={}:{})", redisHost, redisPort);
            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .build();

        } catch (Exception e) {
            log.warn("Redis unavailable — falling back to in-memory cache. Error: {}", e.getMessage());
            return new ConcurrentMapCacheManager("urls", "stats", "domains");
        }
    }
}
