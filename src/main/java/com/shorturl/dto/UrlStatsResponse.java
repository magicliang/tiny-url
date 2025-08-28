package com.shorturl.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for URL statistics
 */
public class UrlStatsResponse {

    private String shortUrl;
    private String longUrl;
    private Long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;

    // Constructors
    public UrlStatsResponse() {}

    public UrlStatsResponse(String shortUrl, String longUrl, Long clickCount, 
                           LocalDateTime createdAt, LocalDateTime expiresAt, boolean isExpired) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isExpired = isExpired;
    }

    // Getters and Setters
    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    @Override
    public String toString() {
        return "UrlStatsResponse{" +
                "shortUrl='" + shortUrl + '\'' +
                ", longUrl='" + longUrl + '\'' +
                ", clickCount=" + clickCount +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", isExpired=" + isExpired +
                '}';
    }
}