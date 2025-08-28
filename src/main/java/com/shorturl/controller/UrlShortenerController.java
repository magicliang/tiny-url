package com.shorturl.controller;

import com.shorturl.dto.ShortenUrlRequest;
import com.shorturl.dto.ShortenUrlResponse;
import com.shorturl.dto.UrlStatsResponse;
import com.shorturl.entity.UrlMapping;
import com.shorturl.exception.UrlNotFoundException;
import com.shorturl.service.UrlShortenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for URL shortening operations
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);

    @Autowired
    private UrlShortenerService urlShortenerService;

    /**
     * Shorten a long URL
     */
@PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request,
                                                        HttpServletRequest httpRequest) {
        logger.info("Received request to shorten URL: {}", request.getUrl());
        
        try {
            // Validate URL format
            if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            // Basic URL validation
            String url = request.getUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            ShortenUrlResponse response = urlShortenerService.shortenUrl(request);
            
            // Build full short URL with domain
            String baseUrl = getBaseUrl(httpRequest);
            response.setShortUrl(baseUrl + "/" + response.getShortUrl());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid URL format: {}", request.getUrl(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error shortening URL: {}", request.getUrl(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Redirect to original URL
     */
    @GetMapping("/{shortUrl}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortUrl) {
        logger.info("Received request to expand short URL: {}", shortUrl);
        
        Optional<String> longUrl = urlShortenerService.expandUrl(shortUrl);
        if (longUrl.isPresent()) {
            logger.info("Redirecting {} to {}", shortUrl, longUrl.get());
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(longUrl.get());
            redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            return redirectView;
        } else {
            logger.warn("Short URL not found: {}", shortUrl);
            throw new UrlNotFoundException("Short URL not found: " + shortUrl);
        }
    }

    /**
     * Get URL statistics
     */
    @GetMapping("/stats/{shortUrl}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortUrl) {
        logger.info("Received request for stats of short URL: {}", shortUrl);
        
        Optional<UrlStatsResponse> stats = urlShortenerService.getUrlStats(shortUrl);
        if (stats.isPresent()) {
            return ResponseEntity.ok(stats.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all URLs (admin endpoint)
     */
    @GetMapping("/admin/urls")
    public ResponseEntity<List<UrlMapping>> getAllUrls() {
        logger.info("Received request to get all URLs");
        List<UrlMapping> urls = urlShortenerService.getAllUrls();
        return ResponseEntity.ok(urls);
    }

    /**
     * Delete a URL
     */
    @DeleteMapping("/admin/urls/{shortUrl}")
    public ResponseEntity<Map<String, String>> deleteUrl(@PathVariable String shortUrl) {
        logger.info("Received request to delete short URL: {}", shortUrl);
        
        boolean deleted = urlShortenerService.deleteUrl(shortUrl);
        Map<String, String> response = new HashMap<>();
        
        if (deleted) {
            response.put("message", "URL deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "URL not found");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cleanup expired URLs
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredUrls() {
        logger.info("Received request to cleanup expired URLs");
        
        int deletedCount = urlShortenerService.cleanupExpiredUrls();
        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", "Cleanup completed successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get system statistics
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        logger.info("Received request for system statistics");
        
        long[] stats = urlShortenerService.getTotalStats();
        Map<String, Object> response = new HashMap<>();
        response.put("totalUrls", stats[0]);
        response.put("totalClicks", stats[1]);
        response.put("urlsCreatedToday", stats[2]);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "URL Shortener");
        return ResponseEntity.ok(response);
    }

    /**
     * Get base URL from request
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath).append("/api/v1");
        return url.toString();
    }

    /**
     * Global exception handler
     */
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUrlNotFoundException(UrlNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        logger.error("Unexpected error", e);
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}