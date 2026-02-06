package com.yk.url_shortener.service;

import com.yk.url_shortener.dto.DomainMetrics;
import com.yk.url_shortener.model.Url;
import com.yk.url_shortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final UrlRepository urlRepository;

    /**
     * Base URL for the application (e.g., "http://localhost:8081")
     * This is read from application.properties
     */
    @Value("${app.base.url:http://localhost:8081}")
    private String baseUrl;

    /**
     * Length of the short code to generate
     */
    private static final int SHORT_CODE_LENGTH = 7;

    /**
     * Shorten a long URL
     *
     * Algorithm:
     * 1. Check if this URL has already been shortened - if yes, return existing short code
     * 2. Generate a unique short code using hash-based approach
     * 3. If collision occurs (very rare), try again with modified input
     * 4. Save the mapping and return
     *
     * @param longUrl The long URL to shorten
     * @return The created or existing Url object
     */
    public Url shortenUrl(String longUrl) {
        // Check if URL already exists in our database
        Optional<Url> existingUrl = urlRepository.findByLongUrl(longUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        String shortCode = generateShortCode(longUrl);

        int attempt = 0;
        while (urlRepository.existsByShortCode(shortCode)) {
            shortCode = generateShortCode(longUrl + attempt);
            attempt++;
        }

        Url url = Url.builder()
                .shortCode(shortCode)
                .longUrl(longUrl)
                .createdAt(LocalDateTime.now())
                .accessCount(0L)
                .build();

        return urlRepository.save(url);
    }

    /**
     * Get the original long URL from a short code
     *
     * @param shortCode The short code to look up
     * @return Optional containing the Url if found
     */
    public Optional<Url> getOriginalUrl(String shortCode) {
        return urlRepository.findByShortCode(shortCode);
    }

    /**
     * Increment the access count for a URL
     * This tracks how many times a short URL has been used
     *
     * @param shortCode The short code to update
     */
    public void incrementAccessCount(String shortCode) {
        urlRepository.findByShortCode(shortCode).ifPresent(url -> {
            url.setAccessCount(url.getAccessCount() + 1);
            urlRepository.save(url);
        });
    }

    /**
     * Generate a short code from a long URL using hashing
     *
     * Algorithm:
     * 1. Use SHA-256 to hash the URL - this creates a unique fingerprint
     * 2. Encode the hash using Base64 - this creates URL-safe characters
     * 3. Take the first 7 characters as our short code
     *
     * Why this approach?
     * - Deterministic: Same URL always generates same hash (before collision check)
     * - Fast: Hashing is very quick
     * - Distributed: Hash values are well-distributed, reducing collisions
     *
     * Alternative approaches:
     * - Random generation (what we use if collision occurs)
     * - Counter-based (like auto-increment ID)
     * - Custom encoding (like Base62)
     *
     * @param url The URL to hash
     * @return A 7-character short code
     */
    private String generateShortCode(String url) {
        try {
            // Create SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));

            // Encode to Base64 and make it URL-safe
            String encoded = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

            // Take first 7 characters
            // Remove any special characters that might cause issues
            String shortCode = encoded.substring(0, SHORT_CODE_LENGTH)
                    .replace("-", "a")
                    .replace("_", "b");

            return shortCode;

        } catch (NoSuchAlgorithmException e) {
            // Fallback to timestamp-based generation if SHA-256 is not available
            return String.valueOf(System.currentTimeMillis()).substring(6);
        }
    }

    /**
     * Build the complete short URL
     *
     * @param shortCode The short code
     * @return Complete URL (e.g., "http://localhost:8081/abc123")
     */
    public String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }

    /**
     * Extract domain name from a URL
     *
     * Examples:
     * - https://www.youtube.com/watch?v=abc -> youtube.com
     * - https://stackoverflow.com/questions/123 -> stackoverflow.com
     * - https://en.wikipedia.org/wiki/Java -> wikipedia.org
     * - https://www.udemy.com/course/java -> udemy.com
     *
     * @param url The full URL
     * @return The domain name (without www.)
     */
    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host == null) {
                return "unknown";
            }

            // Remove "www." prefix if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            return host;

        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get top 3 domains that have been shortened the most
     *
     * Algorithm:
     * 1. Get all URLs from repository
     * 2. Extract domain from each URL
     * 3. Count occurrences of each domain
     * 4. Sort by count in descending order
     * 5. Take top 3
     *
     * Example output:
     * [
     *   { "domain": "udemy.com", "count": 6 },
     *   { "domain": "youtube.com", "count": 4 },
     *   { "domain": "wikipedia.org", "count": 2 }
     * ]
     *
     * @return List of top 3 domains with their counts
     */
    public List<DomainMetrics> getTop3Domains() {
        Collection<Url> allUrls = urlRepository.findAll();

        // Count occurrences of each domain
        Map<String, Long> domainCounts = allUrls.stream()
                .map(url -> extractDomain(url.getLongUrl()))
                .collect(Collectors.groupingBy(
                        domain -> domain,
                        Collectors.counting()
                ));

        // Convert to DomainMetrics objects, sort by count descending, and take top 3
        return domainCounts.entrySet().stream()
                .map(entry -> DomainMetrics.builder()
                        .domain(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount())) // Sort descending
                .limit(3) // Take top 3
                .collect(Collectors.toList());
    }
}
