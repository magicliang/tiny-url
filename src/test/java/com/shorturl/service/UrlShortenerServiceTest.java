package com.shorturl.service;

import com.shorturl.dto.ShortenUrlRequest;
import com.shorturl.dto.ShortenUrlResponse;
import com.shorturl.dto.UrlStatsResponse;
import com.shorturl.entity.UrlMapping;
import com.shorturl.exception.UrlAlreadyExistsException;
import com.shorturl.repository.UrlMappingRepository;
import com.shorturl.service.impl.UrlShortenerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UrlShortenerService
 */
@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @InjectMocks
    private UrlShortenerServiceImpl urlShortenerService;

    private UrlMapping testUrlMapping;
    private ShortenUrlRequest testRequest;

    @BeforeEach
    void setUp() {
        testUrlMapping = new UrlMapping("abc123", "https://www.example.com");
        testUrlMapping.setId(1L);
        testUrlMapping.setClickCount(5L);
        testUrlMapping.setCreatedAt(LocalDateTime.now());

        testRequest = new ShortenUrlRequest("https://www.example.com");
    }

    @Test
    void testShortenUrl_Success() {
        // Given
        when(urlMappingRepository.findByLongUrl(anyString())).thenReturn(Optional.empty());
        when(urlMappingRepository.existsByShortUrl(anyString())).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

        // When
        ShortenUrlResponse response = urlShortenerService.shortenUrl(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("abc123", response.getShortUrl());
        assertEquals("https://www.example.com", response.getLongUrl());
        assertNotNull(response.getCreatedAt());
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void testShortenUrl_ExistingUrl() {
        // Given
        when(urlMappingRepository.findByLongUrl(anyString())).thenReturn(Optional.of(testUrlMapping));

        // When
        ShortenUrlResponse response = urlShortenerService.shortenUrl(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("abc123", response.getShortUrl());
        assertEquals("https://www.example.com", response.getLongUrl());
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void testShortenUrl_CustomAlias_Success() {
        // Given
        testRequest.setCustomAlias("custom");
        when(urlMappingRepository.findByLongUrl(anyString())).thenReturn(Optional.empty());
        when(urlMappingRepository.existsByShortUrl("custom")).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(testUrlMapping);

        // When
        ShortenUrlResponse response = urlShortenerService.shortenUrl(testRequest);

        // Then
        assertNotNull(response);
        verify(urlMappingRepository).existsByShortUrl("custom");
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void testShortenUrl_CustomAlias_AlreadyExists() {
        // Given
        testRequest.setCustomAlias("custom");
        when(urlMappingRepository.findByLongUrl(anyString())).thenReturn(Optional.empty());
        when(urlMappingRepository.existsByShortUrl("custom")).thenReturn(true);

        // When & Then
        assertThrows(UrlAlreadyExistsException.class, () -> {
            urlShortenerService.shortenUrl(testRequest);
        });
    }

    @Test
    void testExpandUrl_Success() {
        // Given
        when(urlMappingRepository.findByShortUrl("abc123")).thenReturn(Optional.of(testUrlMapping));
        when(urlMappingRepository.incrementClickCount("abc123")).thenReturn(1);

        // When
        Optional<String> result = urlShortenerService.expandUrl("abc123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("https://www.example.com", result.get());
        verify(urlMappingRepository).incrementClickCount("abc123");
    }

    @Test
    void testExpandUrl_NotFound() {
        // Given
        when(urlMappingRepository.findByShortUrl("notfound")).thenReturn(Optional.empty());

        // When
        Optional<String> result = urlShortenerService.expandUrl("notfound");

        // Then
        assertFalse(result.isPresent());
        verify(urlMappingRepository, never()).incrementClickCount(anyString());
    }

    @Test
    void testExpandUrl_Expired() {
        // Given
        testUrlMapping.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(urlMappingRepository.findByShortUrl("abc123")).thenReturn(Optional.of(testUrlMapping));

        // When
        Optional<String> result = urlShortenerService.expandUrl("abc123");

        // Then
        assertFalse(result.isPresent());
        verify(urlMappingRepository, never()).incrementClickCount(anyString());
    }

    @Test
    void testGetUrlStats_Success() {
        // Given
        when(urlMappingRepository.findByShortUrl("abc123")).thenReturn(Optional.of(testUrlMapping));

        // When
        Optional<UrlStatsResponse> result = urlShortenerService.getUrlStats("abc123");

        // Then
        assertTrue(result.isPresent());
        UrlStatsResponse stats = result.get();
        assertEquals("abc123", stats.getShortUrl());
        assertEquals("https://www.example.com", stats.getLongUrl());
        assertEquals(5L, stats.getClickCount());
        assertFalse(stats.isExpired());
    }

    @Test
    void testGetUrlStats_NotFound() {
        // Given
        when(urlMappingRepository.findByShortUrl("notfound")).thenReturn(Optional.empty());

        // When
        Optional<UrlStatsResponse> result = urlShortenerService.getUrlStats("notfound");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllUrls() {
        // Given
        List<UrlMapping> urlMappings = Arrays.asList(testUrlMapping);
        when(urlMappingRepository.findAll()).thenReturn(urlMappings);

        // When
        List<UrlMapping> result = urlShortenerService.getAllUrls();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUrlMapping, result.get(0));
    }

    @Test
    void testDeleteUrl_Success() {
        // Given
        when(urlMappingRepository.findByShortUrl("abc123")).thenReturn(Optional.of(testUrlMapping));

        // When
        boolean result = urlShortenerService.deleteUrl("abc123");

        // Then
        assertTrue(result);
        verify(urlMappingRepository).delete(testUrlMapping);
    }

    @Test
    void testDeleteUrl_NotFound() {
        // Given
        when(urlMappingRepository.findByShortUrl("notfound")).thenReturn(Optional.empty());

        // When
        boolean result = urlShortenerService.deleteUrl("notfound");

        // Then
        assertFalse(result);
        verify(urlMappingRepository, never()).delete(any(UrlMapping.class));
    }

    @Test
    void testCleanupExpiredUrls() {
        // Given
        when(urlMappingRepository.deleteExpiredUrls(any(LocalDateTime.class))).thenReturn(3);

        // When
        int result = urlShortenerService.cleanupExpiredUrls();

        // Then
        assertEquals(3, result);
        verify(urlMappingRepository).deleteExpiredUrls(any(LocalDateTime.class));
    }

    @Test
    void testUrlExists() {
        // Given
        when(urlMappingRepository.existsByShortUrl("abc123")).thenReturn(true);
        when(urlMappingRepository.existsByShortUrl("notfound")).thenReturn(false);

        // When & Then
        assertTrue(urlShortenerService.urlExists("abc123"));
        assertFalse(urlShortenerService.urlExists("notfound"));
    }

    @Test
    void testGetTotalStats() {
        // Given
        when(urlMappingRepository.countTotalUrls()).thenReturn(100L);
        when(urlMappingRepository.getTotalClickCount()).thenReturn(1500L);
        when(urlMappingRepository.countUrlsCreatedToday()).thenReturn(10L);

        // When
        long[] result = urlShortenerService.getTotalStats();

        // Then
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(100L, result[0]); // total URLs
        assertEquals(1500L, result[1]); // total clicks
        assertEquals(10L, result[2]); // URLs created today
    }

    @Test
    void testGetTotalStats_NullClickCount() {
        // Given
        when(urlMappingRepository.countTotalUrls()).thenReturn(100L);
        when(urlMappingRepository.getTotalClickCount()).thenReturn(null);
        when(urlMappingRepository.countUrlsCreatedToday()).thenReturn(10L);

        // When
        long[] result = urlShortenerService.getTotalStats();

        // Then
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(100L, result[0]); // total URLs
        assertEquals(0L, result[1]); // total clicks (null converted to 0)
        assertEquals(10L, result[2]); // URLs created today
    }
}