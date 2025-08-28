package com.shorturl.service;

import com.shorturl.dto.ShortenUrlRequest;
import com.shorturl.dto.ShortenUrlResponse;
import com.shorturl.dto.UrlStatsResponse;
import com.shorturl.entity.UrlMapping;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for URL shortening operations
 */
public interface UrlShortenerService {

    /**
     * Shorten a long URL
     * 
     * @param request the shorten URL request
     * @return the shortened URL response
     */
    ShortenUrlResponse shortenUrl(ShortenUrlRequest request);

    /**
     * Expand a short URL to get the original long URL
     * 
     * @param shortUrl the short URL
     * @return the original long URL if found
     */
    Optional<String> expandUrl(String shortUrl);

    /**
     * Get URL statistics
     * 
     * @param shortUrl the short URL
     * @return URL statistics if found
     */
    Optional<UrlStatsResponse> getUrlStats(String shortUrl);

    /**
     * Get all URL mappings (for admin purposes)
     * 
     * @return list of all URL mappings
     */
    List<UrlMapping> getAllUrls();

    /**
     * Delete a URL mapping
     * 
     * @param shortUrl the short URL to delete
     * @return true if deleted successfully
     */
    boolean deleteUrl(String shortUrl);

    /**
     * Clean up expired URLs
     * 
     * @return number of expired URLs deleted
     */
    int cleanupExpiredUrls();

    /**
     * Check if a short URL exists
     * 
     * @param shortUrl the short URL to check
     * @return true if exists
     */
    boolean urlExists(String shortUrl);

    /**
     * Get total statistics
     * 
     * @return array containing [totalUrls, totalClicks, urlsToday]
     */
    long[] getTotalStats();
}