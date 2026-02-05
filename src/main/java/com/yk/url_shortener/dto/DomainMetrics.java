package com.yk.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainMetrics {

    /**
     * Domain name (e.g., "youtube.com", "stackoverflow.com")
     */
    private String domain;

    /**
     * Number of times URLs from this domain have been shortened
     */
    private Long count;
}
