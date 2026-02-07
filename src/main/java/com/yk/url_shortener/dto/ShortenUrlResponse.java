package com.yk.url_shortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing shortened URL details")
public class ShortenUrlResponse {

    @Schema(description = "Original long URL", example = "https://www.example.com/very/long/url/path")
    private String longUrl;

    @Schema(description = "Generated short code (7 characters)", example = "xY7zK3m")
    private String shortCode;

    @Schema(description = "Complete shortened URL", example = "http://localhost:8081/xY7zK3m")
    private String shortUrl;

    @Schema(description = "Timestamp when the URL was shortened", example = "2026-02-07T19:30:00")
    private LocalDateTime createdAt;
}
