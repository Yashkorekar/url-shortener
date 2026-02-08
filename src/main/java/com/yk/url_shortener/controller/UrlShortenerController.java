package com.yk.url_shortener.controller;

import com.yk.url_shortener.dto.DomainMetrics;
import com.yk.url_shortener.dto.ShortenUrlRequest;
import com.yk.url_shortener.dto.ShortenUrlResponse;
import com.yk.url_shortener.dto.UrlStatsResponse;
import com.yk.url_shortener.model.Url;
import com.yk.url_shortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "APIs for URL shortening, redirection, and analytics")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @Operation(
        summary = "Shorten a URL",
        description = "Takes a long URL and returns a shortened version. If the URL was previously shortened, returns the existing short code instead of creating a new one."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "URL successfully shortened",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ShortenUrlResponse.class),
                examples = @ExampleObject(
                    value = "{\"longUrl\":\"https://www.example.com/very/long/url\",\"shortCode\":\"xY7zK3m\",\"shortUrl\":\"http://localhost:8081/xY7zK3m\",\"createdAt\":\"2026-02-07T19:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid URL format or validation error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "URL to be shortened",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        value = "{\"url\":\"https://www.example.com/very/long/url/path\"}"
                    )
                )
            )
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest httpRequest) {

        Url url = urlShortenerService.shortenUrl(request.getUrl());

        // Build base URL from the actual request (works on any domain)
        String baseUrl = getBaseUrl(httpRequest);

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .longUrl(url.getLongUrl())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .createdAt(url.getCreatedAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Redirect to original URL",
        description = "Redirects from a short code to the original long URL. Also increments the access counter.",
        parameters = @Parameter(
            name = "shortCode",
            description = "The 7-character short code",
            example = "xY7zK3m",
            required = true
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "302",
            description = "Redirect to original URL"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Short code not found",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
        )
    })
    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public ResponseEntity<?> redirect(
            @PathVariable
            @Parameter(description = "Short code (alphanumeric, 7 characters)", example = "xY7zK3m")
            String shortCode) {

        Optional<Url> urlOptional = urlShortenerService.getOriginalUrl(shortCode);

        if (urlOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("URL not found for short code: " + shortCode);
        }

        Url url = urlOptional.get();
        urlShortenerService.incrementAccessCount(shortCode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create(url.getLongUrl()))
                .build();
    }

    @Operation(
        summary = "Get URL statistics",
        description = "Returns detailed statistics for a shortened URL including access count and creation time.",
        parameters = @Parameter(
            name = "shortCode",
            description = "The short code to get statistics for",
            example = "xY7zK3m",
            required = true
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UrlStatsResponse.class),
                examples = @ExampleObject(
                    value = "{\"shortCode\":\"xY7zK3m\",\"longUrl\":\"https://www.example.com\",\"shortUrl\":\"http://localhost:8081/xY7zK3m\",\"createdAt\":\"2026-02-07T19:30:00\",\"accessCount\":42}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Short code not found",
            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
        )
    })
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getStats(
            @PathVariable
            @Parameter(description = "Short code to get statistics for", example = "xY7zK3m")
            String shortCode,
            HttpServletRequest httpRequest) {

        Optional<Url> urlOptional = urlShortenerService.getOriginalUrl(shortCode);

        if (urlOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("URL not found for short code: " + shortCode);
        }

        Url url = urlOptional.get();

        // Build base URL from the actual request
        String baseUrl = getBaseUrl(httpRequest);

        UrlStatsResponse response = UrlStatsResponse.builder()
                .shortCode(url.getShortCode())
                .longUrl(url.getLongUrl())
                .shortUrl(baseUrl + "/" + url.getShortCode())
                .createdAt(url.getCreatedAt())
                .accessCount(url.getAccessCount())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get top 3 domains",
        description = "Returns the top 3 domain names that have been shortened the most number of times. Useful for analytics and understanding usage patterns."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Domain metrics retrieved successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = DomainMetrics.class),
                examples = @ExampleObject(
                    value = "[{\"domain\":\"udemy.com\",\"count\":6},{\"domain\":\"youtube.com\",\"count\":4},{\"domain\":\"wikipedia.org\",\"count\":2}]"
                )
            )
        )
    })
    @GetMapping("/api/metrics/domains")
    public ResponseEntity<List<DomainMetrics>> getTopDomains() {
        List<DomainMetrics> topDomains = urlShortenerService.getTop3Domains();
        return ResponseEntity.ok(topDomains);
    }

    /**
     * Helper method to build the base URL from the HTTP request
     * This makes the short URLs work on any domain (localhost, Render, etc.)
     *
     * Examples:
     * - Local: http://localhost:8081
     * - Render: https://url-shortener-0l0j.onrender.com
     * - Custom domain: https://short.yourdomain.com
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();             // http or https
        String serverName = request.getServerName();     // hostname
        int serverPort = request.getServerPort();        // port

        // Build base URL
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // Only add port if it's not the default port for the scheme
        if ((scheme.equals("http") && serverPort != 80) ||
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        return baseUrl.toString();
    }
}
