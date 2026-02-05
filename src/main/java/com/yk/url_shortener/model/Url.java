package com.yk.url_shortener.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    private String shortCode;

    private String longUrl;

    private LocalDateTime createdAt;

    private Long accessCount;
}
