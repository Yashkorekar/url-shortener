package com.yk.url_shortener.service;

import com.yk.url_shortener.dto.DomainMetrics;
import com.yk.url_shortener.model.Url;
import com.yk.url_shortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class UrlShortenerServiceTest {

    private UrlShortenerService urlShortenerService;

    @Autowired
    private UrlRepository urlRepository;

    @BeforeEach
    void setUp() {
        urlRepository.deleteAll(); // Clean database before each test
        urlShortenerService = new UrlShortenerService(urlRepository);
    }

    // ...existing code...

    @Test
    @DisplayName("Test 1: Should shorten URL and return valid short code")
    void testShortenUrl() {
        String longUrl = "https://www.google.com";

        Url result = urlShortenerService.shortenUrl(longUrl);

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getShortCode(), "Short code should not be null");
        assertEquals(7, result.getShortCode().length(), "Short code should be 7 characters long");
        assertEquals(longUrl, result.getLongUrl(), "Long URL should match");
        assertEquals(0L, result.getAccessCount(), "Access count should be 0");
    }

    @Test
    @DisplayName("Test 2: Should return same short code for same URL")
    void testSameUrlReturnsSameShortCode() {
        String longUrl = "https://www.youtube.com/watch?v=abc123";

        Url firstResult = urlShortenerService.shortenUrl(longUrl);
        Url secondResult = urlShortenerService.shortenUrl(longUrl);

        assertNotNull(firstResult.getShortCode(), "First short code should not be null");
        assertNotNull(secondResult.getShortCode(), "Second short code should not be null");
        assertEquals(firstResult.getShortCode(), secondResult.getShortCode(),
                "Same URL should produce same short code");
    }

    @Test
    @DisplayName("Test 3: Should retrieve original URL by short code")
    void testGetOriginalUrl() {
        String longUrl = "https://www.stackoverflow.com/questions/123456";
        Url shortened = urlShortenerService.shortenUrl(longUrl);
        String shortCode = shortened.getShortCode();

        Optional<Url> result = urlShortenerService.getOriginalUrl(shortCode);

        assertTrue(result.isPresent(), "URL should be found");
        assertEquals(longUrl, result.get().getLongUrl(), "Original URL should match");
        assertEquals(shortCode, result.get().getShortCode(), "Short code should match");
    }

    @Test
    @DisplayName("Test 4: Should calculate top 3 domains correctly")
    void testGetTop3Domains() {
        urlShortenerService.shortenUrl("https://www.youtube.com/video1");
        urlShortenerService.shortenUrl("https://www.youtube.com/video2");
        urlShortenerService.shortenUrl("https://www.youtube.com/video3");
        urlShortenerService.shortenUrl("https://www.youtube.com/video4");

        urlShortenerService.shortenUrl("https://www.udemy.com/course1");
        urlShortenerService.shortenUrl("https://www.udemy.com/course2");
        urlShortenerService.shortenUrl("https://www.udemy.com/course3");
        urlShortenerService.shortenUrl("https://www.udemy.com/course4");
        urlShortenerService.shortenUrl("https://www.udemy.com/course5");
        urlShortenerService.shortenUrl("https://www.udemy.com/course6");

        urlShortenerService.shortenUrl("https://en.wikipedia.org/wiki/Java");
        urlShortenerService.shortenUrl("https://en.wikipedia.org/wiki/Python");

        List<DomainMetrics> topDomains = urlShortenerService.getTop3Domains();

        // Then: Should return top 3 domains in correct order
        assertNotNull(topDomains, "Top domains should not be null");
        assertEquals(3, topDomains.size(), "Should return exactly 3 domains");

        // First should be udemy.com with count 6
        assertEquals("udemy.com", topDomains.get(0).getDomain(), "First domain should be udemy.com");
        assertEquals(6L, topDomains.get(0).getCount(), "Udemy count should be 6");

        // Second should be youtube.com with count 4
        assertEquals("youtube.com", topDomains.get(1).getDomain(), "Second domain should be youtube.com");
        assertEquals(4L, topDomains.get(1).getCount(), "YouTube count should be 4");

        // Third should be en.wikipedia.org with count 2
        assertEquals("en.wikipedia.org", topDomains.get(2).getDomain(), "Third domain should be en.wikipedia.org");
        assertEquals(2L, topDomains.get(2).getCount(), "Wikipedia count should be 2");
    }
}
