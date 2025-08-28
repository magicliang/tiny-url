package com.shorturl.service.impl;

import com.shorturl.dto.ShortenUrlRequest;
import com.shorturl.dto.ShortenUrlResponse;
import com.shorturl.dto.UrlStatsResponse;
import com.shorturl.entity.UrlMapping;
import com.shorturl.exception.UrlNotFoundException;
import com.shorturl.exception.UrlAlreadyExistsException;
import com.shorturl.repository.UrlMappingRepository;
import com.shorturl.service.UrlShortenerService;
import com.shorturl.util.Base62Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of URL shortener service
 */
@Service
@Transactional
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerServiceImpl.class);
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int SHORT_URL_LENGTH = 7;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Override
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        logger.info("Shortening URL: {}", request.getUrl());

        // Check if URL already exists
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByLongUrl(request.getUrl());
        if (existingMapping.isPresent() && !existingMapping.get().isExpired()) {
            UrlMapping mapping = existingMapping.get();
            logger.info("URL already exists with short URL: {}", mapping.getShortUrl());
            return new ShortenUrlResponse(mapping.getShortUrl(), mapping.getLongUrl(), 
                                        mapping.getCreatedAt(), mapping.getExpiresAt());
        }

        String shortUrl;
        if (StringUtils.hasText(request.getCustomAlias())) {
            // Use custom alias
            shortUrl = request.getCustomAlias();
            if (urlMappingRepository.existsByShortUrl(shortUrl)) {
                throw new UrlAlreadyExistsException("Custom alias already exists: " + shortUrl);
            }
        } else {
            // Generate random short URL
            shortUrl = generateUniqueShortUrl();
        }

        UrlMapping urlMapping = new UrlMapping(shortUrl, request.getUrl(), request.getExpiresAt());
        urlMapping = urlMappingRepository.save(urlMapping);

        logger.info("Successfully created short URL: {} for long URL: {}", shortUrl, request.getUrl());
        return new ShortenUrlResponse(urlMapping.getShortUrl(), urlMapping.getLongUrl(), 
                                    urlMapping.getCreatedAt(), urlMapping.getExpiresAt());
    }

    @Override
    @Cacheable(value = "urlMappings", key = "#shortUrl")
    public Optional<String> expandUrl(String shortUrl) {
        logger.debug("Expanding short URL: {}", shortUrl);

        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping.isPresent()) {
            UrlMapping mapping = urlMapping.get();
            if (mapping.isExpired()) {
                logger.warn("Short URL {} has expired", shortUrl);
                return Optional.empty();
            }

            // Increment click count asynchronously
            incrementClickCountAsync(shortUrl);
            
            logger.debug("Successfully expanded short URL: {} to: {}", shortUrl, mapping.getLongUrl());
            return Optional.of(mapping.getLongUrl());
        }

        logger.warn("Short URL not found: {}", shortUrl);
        return Optional.empty();
    }

    @Override
    @Cacheable(value = "urlStats", key = "#shortUrl")
    public Optional<UrlStatsResponse> getUrlStats(String shortUrl) {
        logger.debug("Getting stats for short URL: {}", shortUrl);

        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping.isPresent()) {
            UrlMapping mapping = urlMapping.get();
            UrlStatsResponse stats = new UrlStatsResponse(
                mapping.getShortUrl(),
                mapping.getLongUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt(),
                mapping.isExpired()
            );
            return Optional.of(stats);
        }

        return Optional.empty();
    }

    @Override
    public List<UrlMapping> getAllUrls() {
        logger.debug("Getting all URL mappings");
        return urlMappingRepository.findAll();
    }

    @Override
    @CacheEvict(value = {"urlMappings", "urlStats"}, key = "#shortUrl")
    public boolean deleteUrl(String shortUrl) {
        logger.info("Deleting short URL: {}", shortUrl);

        Optional<UrlMapping> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping.isPresent()) {
            urlMappingRepository.delete(urlMapping.get());
            logger.info("Successfully deleted short URL: {}", shortUrl);
            return true;
        }

        logger.warn("Short URL not found for deletion: {}", shortUrl);
        return false;
    }

    @Override
    public int cleanupExpiredUrls() {
        logger.info("Cleaning up expired URLs");
        int deletedCount = urlMappingRepository.deleteExpiredUrls(LocalDateTime.now());
        logger.info("Cleaned up {} expired URLs", deletedCount);
        return deletedCount;
    }

    @Override
    public boolean urlExists(String shortUrl) {
        return urlMappingRepository.existsByShortUrl(shortUrl);
    }

    @Override
    public long[] getTotalStats() {
        long totalUrls = urlMappingRepository.countTotalUrls();
        Long totalClicks = urlMappingRepository.getTotalClickCount();
        long urlsToday = urlMappingRepository.countUrlsCreatedToday();
        
        return new long[]{totalUrls, totalClicks != null ? totalClicks : 0L, urlsToday};
    }

    /**
     * Generate a unique short URL
     */
    private String generateUniqueShortUrl() {
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            String shortUrl = generateRandomShortUrl();
            if (!urlMappingRepository.existsByShortUrl(shortUrl)) {
                return shortUrl;
            }
        }
        
        // If we can't generate a unique URL after max attempts, use timestamp-based approach
        long timestamp = System.currentTimeMillis();
        String shortUrl = Base62Encoder.encode(timestamp).substring(0, Math.min(SHORT_URL_LENGTH, 
                                                Base62Encoder.encode(timestamp).length()));
        
        // Ensure uniqueness by appending random characters if needed
        while (urlMappingRepository.existsByShortUrl(shortUrl)) {
            shortUrl = shortUrl + Base62Encoder.encode(ThreadLocalRandom.current().nextLong()).charAt(0);
            if (shortUrl.length() > 10) {
                shortUrl = shortUrl.substring(0, 10);
            }
        }
        
        return shortUrl;
    }

    /**
     * Generate a random short URL
     */
    private String generateRandomShortUrl() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        String encoded = Base62Encoder.encode(randomNumber);
        return encoded.substring(0, Math.min(SHORT_URL_LENGTH, encoded.length()));
    }

    /**
     * Increment click count asynchronously
     */
    private void incrementClickCountAsync(String shortUrl) {
        try {
            urlMappingRepository.incrementClickCount(shortUrl);
        } catch (Exception e) {
            logger.error("Failed to increment click count for short URL: {}", shortUrl, e);
        }
    }
}