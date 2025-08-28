package com.shorturl.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for URL shortening
 */
public class ShortenUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;

    @Size(max = 10, message = "Custom alias cannot exceed 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Custom alias can only contain alphanumeric characters")
    private String customAlias;

    private LocalDateTime expiresAt;

    // Constructors
    public ShortenUrlRequest() {}

    public ShortenUrlRequest(String url) {
        this.url = url;
    }

    public ShortenUrlRequest(String url, String customAlias) {
        this.url = url;
        this.customAlias = customAlias;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "ShortenUrlRequest{" +
                "url='" + url + '\'' +
                ", customAlias='" + customAlias + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}