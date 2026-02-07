package com.yk.url_shortener.dto;

import com.yk.url_shortener.validation.ValidUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for URL shortening")
public class ShortenUrlRequest {

    /**
     * The long URL to be shortened
     * Validation:
     * - Cannot be blank
     * - Must be a valid URL format (http:// or https://)
     * - Must have valid host/domain
     * - Cannot be localhost or private IP
     * - Maximum length: 2048 characters
     */
    @Schema(
        description = "The long URL to be shortened",
        example = "https://www.example.com/very/long/url/path/that/needs/shortening",
        required = true
    )
    @NotBlank(message = "URL cannot be blank")
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    @Pattern(
        regexp = "^(http|https)://.*$",
        message = "URL must start with http:// or https://"
    )
    @ValidUrl(
        message = "Invalid URL format or unreachable domain",
        checkReachability = false
    )
    private String url;
}
