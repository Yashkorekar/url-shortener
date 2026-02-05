package com.yk.url_shortener.repository;

import com.yk.url_shortener.model.Url;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UrlRepository {

    /**
     * In-memory storage for URL mappings
     * Key: shortCode (e.g., "abc123")
     * Value: Url object containing all details
     */
    private final Map<String, Url> urlStorage = new ConcurrentHashMap<>();

    /**
     * Save a URL mapping
     * @param url The URL object to save
     * @return The saved URL object
     */
    public Url save(Url url) {
        urlStorage.put(url.getShortCode(), url);
        return url;
    }

    /**
     * Find a URL by its short code
     * @param shortCode The short code to search for
     * @return Optional containing the URL if found, empty Optional if not found
     *
     * Optional is a Java container object that may or may not contain a non-null value.
     * This helps prevent NullPointerException.
     */
    public Optional<Url> findByShortCode(String shortCode) {
        return Optional.ofNullable(urlStorage.get(shortCode));
    }

    /**
     * Check if a short code already exists
     * @param shortCode The short code to check
     * @return true if exists, false otherwise
     */
    public boolean existsByShortCode(String shortCode) {
        return urlStorage.containsKey(shortCode);
    }

    /**
     * Find a URL by its long URL
     * Useful to check if we've already shortened this URL before
     * @param longUrl The long URL to search for
     * @return Optional containing the URL if found
     */
    public Optional<Url> findByLongUrl(String longUrl) {
        return urlStorage.values().stream()
                .filter(url -> url.getLongUrl().equals(longUrl))
                .findFirst();
    }
}
