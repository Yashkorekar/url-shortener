package com.yk.url_shortener.repository;

import com.yk.url_shortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {

    /**
     * Find a URL by its short code
     * @param shortCode The short code to search for
     * @return Optional containing the URL if found, empty Optional if not found
     *
     * This method is automatically implemented by Spring Data JPA
     * It queries: SELECT * FROM urls WHERE short_code = ?
     */
    Optional<Url> findByShortCode(String shortCode);

    /**
     * Check if a short code already exists
     * @param shortCode The short code to check
     * @return true if exists, false otherwise
     *
     * Spring generates: SELECT COUNT(*) > 0 FROM urls WHERE short_code = ?
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Find a URL by its long URL
     * Useful to check if we've already shortened this URL before
     * @param longUrl The long URL to search for
     * @return Optional containing the URL if found
     *
     * Spring generates: SELECT * FROM urls WHERE long_url = ?
     */
    Optional<Url> findByLongUrl(String longUrl);

    /**
     * Get all stored URLs
     * Inherited from JpaRepository - findAll() is already available
     * Used for metrics calculation
     * No need to declare it - it's already provided by JpaRepository
     */
}
