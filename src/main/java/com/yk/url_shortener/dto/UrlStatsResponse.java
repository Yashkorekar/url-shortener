package com.yk.url_shortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for URL statistics.
 * Returns detailed information about a shortened URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistics for a shortened URL")
public class UrlStatsResponse {

    @Schema(description = "Short code identifier", example = "xY7zK3m")
    private String shortCode;

    @Schema(description = "Original long URL", example = "https://www.example.com/very/long/url")
    private String longUrl;

    @Schema(description = "Complete shortened URL", example = "http://localhost:8081/xY7zK3m")
    private String shortUrl;

    @Schema(description = "When the URL was created", example = "2026-02-07T19:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Number of times the short URL has been accessed", example = "42")
    private Long accessCount;
}
