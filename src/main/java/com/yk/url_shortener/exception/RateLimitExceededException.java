package com.yk.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a client exceeds the rate limit.
 * Results in HTTP 429 Too Many Requests.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String clientIp) {
        super("Rate limit exceeded for IP: " + clientIp + ". Please slow down and try again later.");
    }
}

