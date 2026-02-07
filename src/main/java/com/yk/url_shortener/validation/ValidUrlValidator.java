package com.yk.url_shortener.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Validator implementation for @ValidUrl annotation
 *
 * This performs comprehensive URL validation:
 * 1. Checks if the string is a valid URL
 * 2. Validates the protocol (http/https)
 * 3. Checks for valid host
 * 4. Optionally checks if URL is reachable
 */
@Component
public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    private boolean checkReachability;
    private List<String> allowedProtocols;

    @Override
    public void initialize(ValidUrl constraintAnnotation) {
        this.checkReachability = constraintAnnotation.checkReachability();
        this.allowedProtocols = Arrays.asList(constraintAnnotation.allowedProtocols());
    }

    @Override
    public boolean isValid(String urlString, ConstraintValidatorContext context) {
        // Null or empty is handled by @NotBlank
        if (urlString == null || urlString.trim().isEmpty()) {
            return true;
        }

        try {
            // Step 1: Parse as URI (RFC 3986 compliant)
            URI uri = new URI(urlString);

            // Step 2: Check if it's absolute (has scheme)
            if (!uri.isAbsolute()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "URL must be absolute (include protocol like http:// or https://)"
                ).addConstraintViolation();
                return false;
            }

            // Step 3: Validate protocol/scheme
            String scheme = uri.getScheme().toLowerCase();
            if (!allowedProtocols.contains(scheme)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "URL protocol must be one of: " + allowedProtocols
                ).addConstraintViolation();
                return false;
            }

            // Step 4: Check for valid host
            String host = uri.getHost();
            if (host == null || host.trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "URL must have a valid host/domain"
                ).addConstraintViolation();
                return false;
            }

            // Step 5: Validate as URL (more strict)
            URL url = uri.toURL();

            // Step 6: Additional checks for suspicious patterns
            if (host.contains("..") || host.startsWith(".") || host.endsWith(".")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "URL contains invalid host pattern"
                ).addConstraintViolation();
                return false;
            }

            // Step 7: Check for localhost/internal IPs (optional security check)
            if (isLocalOrPrivateAddress(host)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Cannot shorten localhost or private network URLs"
                ).addConstraintViolation();
                return false;
            }

            // Step 8: Optional reachability check (network call - expensive!)
            if (checkReachability && !isReachable(url)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "URL is not reachable or returns an error"
                ).addConstraintViolation();
                return false;
            }

            return true;

        } catch (MalformedURLException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Malformed URL: " + e.getMessage()
            ).addConstraintViolation();
            return false;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Invalid URL: " + e.getMessage()
            ).addConstraintViolation();
            return false;
        }
    }

    /**
     * Check if the URL points to localhost or private network
     */
    private boolean isLocalOrPrivateAddress(String host) {
        host = host.toLowerCase();

        // Check for localhost variants
        if (host.equals("localhost") ||
            host.equals("127.0.0.1") ||
            host.equals("::1") ||
            host.startsWith("127.") ||
            host.endsWith(".local")) {
            return true;
        }

        // Check for private IP ranges (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
        if (host.matches("^192\\.168\\..*") ||
            host.matches("^10\\..*") ||
            host.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")) {
            return true;
        }

        return false;
    }

    /**
     * Check if URL is reachable by making a HEAD request
     * This is an expensive operation - use sparingly!
     */
    private boolean isReachable(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000); // 3 seconds timeout
            connection.setReadTimeout(3000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            // Accept 2xx and 3xx response codes
            return responseCode >= 200 && responseCode < 400;

        } catch (Exception e) {
            // URL is not reachable
            return false;
        }
    }
}

