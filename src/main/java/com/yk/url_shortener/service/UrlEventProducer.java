package com.yk.url_shortener.service;

import com.yk.url_shortener.dto.UrlAccessedEvent;
import com.yk.url_shortener.dto.UrlCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Event Producer
 *
 * Publishes events to two topics:
 *
 *  Topic: url.created
 *    - Published after a new URL is shortened
 *    - Key: shortCode (ensures all events for the same shortCode go to the same partition)
 *
 *  Topic: url.accessed
 *    - Published on every redirect instead of synchronously hitting the DB
 *    - Key: shortCode
 *    - This makes the redirect endpoint non-blocking — user gets the 302 redirect
 *      immediately, and the access count DB write happens async via the consumer
 *
 * Graceful degradation:
 *    If Kafka is not running, errors are caught and logged.
 *    The app keeps working — URL shortening and redirects still function.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class UrlEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_URL_CREATED  = "url.created";
    private static final String TOPIC_URL_ACCESSED = "url.accessed";

    /**
     * Publish a "url.created" event after a new short URL is saved.
     *
     * @param event the event payload
     */
    public void publishUrlCreated(UrlCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC_URL_CREATED, event.getShortCode(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // Kafka unavailable — log and move on, don't fail the request
                log.error("Failed to publish url.created event for shortCode={}: {}",
                        event.getShortCode(), ex.getMessage());
            } else {
                log.debug("Published url.created | shortCode={} | partition={} | offset={}",
                        event.getShortCode(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * Publish a "url.accessed" event on every redirect.
     * The consumer will process this event and update the access count in DB asynchronously.
     *
     * @param event the event payload
     */
    public void publishUrlAccessed(UrlAccessedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TOPIC_URL_ACCESSED, event.getShortCode(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // Kafka unavailable — fall back to sync DB update handled in controller
                log.error("Failed to publish url.accessed event for shortCode={}: {}",
                        event.getShortCode(), ex.getMessage());
            } else {
                log.debug("Published url.accessed | shortCode={} | partition={} | offset={}",
                        event.getShortCode(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}

