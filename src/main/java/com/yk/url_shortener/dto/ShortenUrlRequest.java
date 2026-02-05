package com.yk.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {

    /**
     * The long URL to be shortened
     * Validation:
     * - Cannot be blank
     * - Must be a valid URL format (http:// or https://)
     */
    @NotBlank(message = "URL cannot be blank")
    @Pattern(
        regexp = "^(http|https)://.*$",
        message = "URL must start with http:// or https://"
    )
    private String url;
}
