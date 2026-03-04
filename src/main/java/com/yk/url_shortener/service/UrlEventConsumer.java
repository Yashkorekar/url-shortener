package com.yk.url_shortener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka Event Consumer — only active when spring.kafka.enabled=true (the default).
 * When spring.kafka.enabled=false this bean is not created at all,
 * so no listener threads are started and no broker connection is attempted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UrlEventConsumer {

    private final UrlShortenerService urlShortenerService;

    /**
     * Consume "url.accessed" events and update access count in DB.
     *
     * groupId = "url-shortener-group"
     *   → All instances of this app share the partition load.
     *   → If you run 3 app instances, each gets 1 partition (3 partitions total).
     */
    @KafkaListener(
            topics = "url.accessed",
            groupId = "url-shortener-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUrlAccessed(Map<String, Object> payload) {
        try {
            String shortCode = (String) payload.get("shortCode");
            if (shortCode == null || shortCode.isBlank()) {
                log.warn("Received url.accessed event with null/blank shortCode — skipping");
                return;
            }

            // This is the async DB write — happens in background, not on the request thread
            urlShortenerService.incrementAccessCount(shortCode);
            log.debug("Access count incremented for shortCode={} via Kafka", shortCode);

        } catch (Exception e) {
            log.error("Error processing url.accessed event: payload={}, error={}", payload, e.getMessage(), e);
            // Do NOT rethrow — a bad message should not crash the consumer thread
        }
    }

    /**
     * Consume "url.created" events for audit/analytics logging.
     * Extend this method to: store audit records, send notifications, feed dashboards.
     */
    @KafkaListener(
            topics = "url.created",
            groupId = "url-shortener-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUrlCreated(Map<String, Object> payload) {
        try {
            String shortCode = (String) payload.get("shortCode");
            String longUrl   = (String) payload.get("longUrl");
            String shortUrl  = (String) payload.get("shortUrl");

            log.info("[AUDIT] New URL created | shortCode={} | longUrl={} | shortUrl={}",
                    shortCode, longUrl, shortUrl);

            // TODO: extend here — e.g., persist to an audit_log table, send Slack notification, etc.

        } catch (Exception e) {
            log.error("Error processing url.created event: payload={}, error={}", payload, e.getMessage(), e);
        }
    }
}

