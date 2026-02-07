package com.yk.url_shortener.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {

    String message() default "Invalid URL format or unreachable URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Whether to check if the URL is actually reachable
     * Default: false (to avoid network calls in validation)
     */
    boolean checkReachability() default false;

    /**
     * Allowed protocols (schemes)
     * Default: http and https
     */
    String[] allowedProtocols() default {"http", "https"};
}

