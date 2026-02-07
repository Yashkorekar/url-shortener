package com.yk.url_shortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Domain metrics showing how many URLs from a domain were shortened")
public class DomainMetrics {

    /**
     * Domain name (e.g., "youtube.com", "stackoverflow.com")
     */
    @Schema(description = "Domain name", example = "youtube.com")
    private String domain;

    /**
     * Number of times URLs from this domain have been shortened
     */
    @Schema(description = "Number of URLs shortened from this domain", example = "42")
    private Long count;
}
