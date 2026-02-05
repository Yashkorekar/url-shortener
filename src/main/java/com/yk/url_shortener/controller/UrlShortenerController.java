package com.yk.url_shortener.controller;

import com.yk.url_shortener.dto.DomainMetrics;
import com.yk.url_shortener.dto.ShortenUrlRequest;
import com.yk.url_shortener.dto.ShortenUrlResponse;
import com.yk.url_shortener.dto.UrlStatsResponse;
import com.yk.url_shortener.model.Url;
import com.yk.url_shortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    /**
     * Endpoint to shorten a URL
     *
     * POST /api/shorten
     * Request Body: {"url": "https://www.example.com/very/long/url"}
     * Response: {"longUrl": "...", "shortCode": "abc123", "shortUrl": "http://localhost:8082/abc123", ...}
     *
     * @param request The request containing the long URL
     * @return Response with the shortened URL details
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {

        Url url = urlShortenerService.shortenUrl(request.getUrl());

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .longUrl(url.getLongUrl())
                .shortCode(url.getShortCode())
                .shortUrl(urlShortenerService.buildShortUrl(url.getShortCode()))
                .createdAt(url.getCreatedAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to redirect short URL to original URL
     *
     * GET /{shortCode}
     * Example: GET /abc123
     * Response: HTTP 302 Redirect to the original URL
     *
     * This is the main functionality - when someone visits the short URL,
     * they get redirected to the original long URL.
     *
     * The regex pattern ensures we only match short codes (alphanumeric),
     * not file paths like index.html or paths with dots/slashes
     *
     * @param shortCode The short code from the URL path
     * @return RedirectView that redirects the browser or 404 if not found
     */
    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {

        Optional<Url> urlOptional = urlShortenerService.getOriginalUrl(shortCode);

        // If URL not found, return 404 error
        if (urlOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("URL not found for short code: " + shortCode);
        }

        Url url = urlOptional.get();
        urlShortenerService.incrementAccessCount(shortCode);

        // Redirect to the original URL
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(url.getLongUrl());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(java.net.URI.create(url.getLongUrl()))
                .build();
    }

    /**
     * Endpoint to get statistics about a shortened URL
     *
     * GET /api/stats/{shortCode}
     * Example: GET /api/stats/abc123
     * Response: {"shortCode": "abc123", "longUrl": "...", "accessCount": 42, ...}
     *
     * This allows users to see how many times their short URL has been accessed.
     *
     * @param shortCode The short code to get stats for
     * @return Statistics about the URL or 404 if not found
     */
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {

        Optional<Url> urlOptional = urlShortenerService.getOriginalUrl(shortCode);

        // If URL not found, return 404 error
        if (urlOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("URL not found for short code: " + shortCode);
        }

        Url url = urlOptional.get();

        UrlStatsResponse response = UrlStatsResponse.builder()
                .shortCode(url.getShortCode())
                .longUrl(url.getLongUrl())
                .shortUrl(urlShortenerService.buildShortUrl(url.getShortCode()))
                .createdAt(url.getCreatedAt())
                .accessCount(url.getAccessCount())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to get domain metrics - top 3 domains that have been shortened the most
     *
     * GET /api/metrics/domains
     * Response: [
     *   {"domain": "udemy.com", "count": 6},
     *   {"domain": "youtube.com", "count": 4},
     *   {"domain": "wikipedia.org", "count": 2}
     * ]
     *
     * This returns the top 3 domain names that have been shortened the most number of times.
     *
     * @return List of top 3 domains with their counts
     */
    @GetMapping("/api/metrics/domains")
    public ResponseEntity<List<DomainMetrics>> getTopDomains() {
        List<DomainMetrics> topDomains = urlShortenerService.getTop3Domains();
        return ResponseEntity.ok(topDomains);
    }
}
