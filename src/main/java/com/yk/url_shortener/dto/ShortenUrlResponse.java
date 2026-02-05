package com.yk.url_shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlResponse {

    private String longUrl;

    private String shortCode;

    private String shortUrl;

    private LocalDateTime createdAt;
}
