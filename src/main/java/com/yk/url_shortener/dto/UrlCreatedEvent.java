package com.yk.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka event published to topic "url.created"
 * whenever a new short URL is created.
 *
 * Consumers of this event can:
 * - Build an audit log
 * - Send a welcome / confirmation notification
 * - Feed a real-time analytics dashboard
 * - Trigger webhook notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlCreatedEvent {

    /** The 7-character short code */
    private String shortCode;

    /** The original long URL that was shortened */
    private String longUrl;

    /** Full short URL e.g. http://localhost:8081/abc1234 */
    private String shortUrl;

    /** When the URL was created */
    private LocalDateTime createdAt;
}

