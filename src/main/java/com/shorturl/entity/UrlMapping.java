package com.shorturl.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * URL Mapping Entity
 * 
 * Represents the mapping between short URLs and long URLs
 */
@Entity
@Table(name = "url_mapping", indexes = {
    @Index(name = "idx_short_url", columnList = "short_url"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url", nullable = false, unique = true, length = 10)
    @NotBlank(message = "Short URL cannot be blank")
    @Size(max = 10, message = "Short URL cannot exceed 10 characters")
    private String shortUrl;

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Long URL cannot be blank")
    private String longUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    // Constructors
    public UrlMapping() {
        this.createdAt = LocalDateTime.now();
    }

    public UrlMapping(String shortUrl, String longUrl) {
        this();
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
    }

    public UrlMapping(String shortUrl, String longUrl, LocalDateTime expiresAt) {
        this(shortUrl, longUrl);
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    // Utility methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    @Override
    public String toString() {
        return "UrlMapping{" +
                "id=" + id +
                ", shortUrl='" + shortUrl + '\'' +
                ", longUrl='" + longUrl + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", clickCount=" + clickCount +
                '}';
    }
}