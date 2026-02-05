package com.yk.url_shortener.dto;

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
public class UrlStatsResponse {

    private String shortCode;

    private String longUrl;

    private String shortUrl;

    private LocalDateTime createdAt;

    private Long accessCount;
}
