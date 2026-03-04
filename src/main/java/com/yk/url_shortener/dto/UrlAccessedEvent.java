package com.yk.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka event published to topic "url.accessed"
 * whenever someone visits a short URL (redirect).
 *
 * Why publish this to Kafka instead of writing DB synchronously?
 * - The redirect endpoint returns to the user IMMEDIATELY (fast!)
 * - The DB write (incrementAccessCount) happens ASYNC via Kafka consumer
 * - Under high traffic (millions of redirects), the DB is protected from
 *   write spikes — Kafka acts as a buffer
 *
 * Consumers of this event can:
 * - Update access count in DB (async, non-blocking)
 * - Feed real-time analytics (clicks per second dashboard)
 * - Build geo/device analytics if extended with more fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlAccessedEvent {

    /** The short code that was accessed */
    private String shortCode;

    /** The original long URL (for analytics without a DB lookup) */
    private String longUrl;

    /** When the access happened */
    private LocalDateTime accessedAt;

    /** Client IP address (for geo/abuse analytics) */
    private String clientIp;
}

