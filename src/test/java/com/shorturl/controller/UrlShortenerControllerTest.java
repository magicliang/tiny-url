package com.shorturl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shorturl.dto.ShortenUrlRequest;
import com.shorturl.dto.ShortenUrlResponse;
import com.shorturl.dto.UrlStatsResponse;
import com.shorturl.entity.UrlMapping;
import com.shorturl.exception.UrlNotFoundException;
import com.shorturl.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UrlShortenerController
 */
@WebMvcTest(UrlShortenerController.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlShortenerService urlShortenerService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShortenUrlRequest testRequest;
    private ShortenUrlResponse testResponse;
    private UrlMapping testUrlMapping;
    private UrlStatsResponse testStatsResponse;

    @BeforeEach
    void setUp() {
        testRequest = new ShortenUrlRequest("https://www.example.com");
        
        testResponse = new ShortenUrlResponse("abc123", "https://www.example.com", LocalDateTime.now());
        
        testUrlMapping = new UrlMapping("abc123", "https://www.example.com");
        testUrlMapping.setId(1L);
        testUrlMapping.setClickCount(5L);
        
        testStatsResponse = new UrlStatsResponse("abc123", "https://www.example.com", 
                                               5L, LocalDateTime.now(), null, false);
    }

    @Test
    void testShortenUrl_Success() throws Exception {
        // Given
        when(urlShortenerService.shortenUrl(any(ShortenUrlRequest.class))).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.longUrl").value("https://www.example.com"));

        verify(urlShortenerService).shortenUrl(any(ShortenUrlRequest.class));
    }

    @Test
    void testShortenUrl_InvalidRequest() throws Exception {
        // Given
        ShortenUrlRequest invalidRequest = new ShortenUrlRequest("");

        // When & Then
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(urlShortenerService, never()).shortenUrl(any(ShortenUrlRequest.class));
    }

    @Test
    void testRedirectToOriginalUrl_Success() throws Exception {
        // Given
        when(urlShortenerService.expandUrl("abc123")).thenReturn(Optional.of("https://www.example.com"));

        // When & Then
        mockMvc.perform(get("/api/v1/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://www.example.com"));

        verify(urlShortenerService).expandUrl("abc123");
    }

    @Test
    void testRedirectToOriginalUrl_NotFound() throws Exception {
        // Given
        when(urlShortenerService.expandUrl("notfound")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/notfound"))
                .andExpect(status().isNotFound());

        verify(urlShortenerService).expandUrl("notfound");
    }

    @Test
    void testGetUrlStats_Success() throws Exception {
        // Given
        when(urlShortenerService.getUrlStats("abc123")).thenReturn(Optional.of(testStatsResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/stats/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("abc123"))
                .andExpect(jsonPath("$.longUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.clickCount").value(5))
                .andExpect(jsonPath("$.expired").value(false));

        verify(urlShortenerService).getUrlStats("abc123");
    }

    @Test
    void testGetUrlStats_NotFound() throws Exception {
        // Given
        when(urlShortenerService.getUrlStats("notfound")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/stats/notfound"))
                .andExpect(status().isNotFound());

        verify(urlShortenerService).getUrlStats("notfound");
    }

    @Test
    void testGetAllUrls_Success() throws Exception {
        // Given
        List<UrlMapping> urlMappings = Arrays.asList(testUrlMapping);
        when(urlShortenerService.getAllUrls()).thenReturn(urlMappings);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].shortUrl").value("abc123"))
                .andExpect(jsonPath("$[0].longUrl").value("https://www.example.com"));

        verify(urlShortenerService).getAllUrls();
    }

    @Test
    void testDeleteUrl_Success() throws Exception {
        // Given
        when(urlShortenerService.deleteUrl("abc123")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/urls/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("URL deleted successfully"));

        verify(urlShortenerService).deleteUrl("abc123");
    }

    @Test
    void testDeleteUrl_NotFound() throws Exception {
        // Given
        when(urlShortenerService.deleteUrl("notfound")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/urls/notfound"))
                .andExpect(status().isNotFound());

        verify(urlShortenerService).deleteUrl("notfound");
    }

    @Test
    void testCleanupExpiredUrls_Success() throws Exception {
        // Given
        when(urlShortenerService.cleanupExpiredUrls()).thenReturn(5);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(5))
                .andExpect(jsonPath("$.message").value("Cleanup completed successfully"));

        verify(urlShortenerService).cleanupExpiredUrls();
    }

    @Test
    void testGetSystemStats_Success() throws Exception {
        // Given
        long[] stats = {100L, 1500L, 10L};
        when(urlShortenerService.getTotalStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUrls").value(100))
                .andExpect(jsonPath("$.totalClicks").value(1500))
                .andExpect(jsonPath("$.urlsCreatedToday").value(10));

        verify(urlShortenerService).getTotalStats();
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("URL Shortener"));
    }

    @Test
    void testShortenUrl_WithCustomAlias() throws Exception {
        // Given
        ShortenUrlRequest requestWithAlias = new ShortenUrlRequest("https://www.example.com");
        requestWithAlias.setCustomAlias("custom");
        
        ShortenUrlResponse responseWithAlias = new ShortenUrlResponse("custom", "https://www.example.com", LocalDateTime.now());
        when(urlShortenerService.shortenUrl(any(ShortenUrlRequest.class))).thenReturn(responseWithAlias);

        // When & Then
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithAlias)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.longUrl").value("https://www.example.com"));

        verify(urlShortenerService).shortenUrl(any(ShortenUrlRequest.class));
    }

    @Test
    void testShortenUrl_InvalidUrl() throws Exception {
        // Given
        ShortenUrlRequest invalidRequest = new ShortenUrlRequest("not-a-valid-url");

        // When & Then
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(urlShortenerService, never()).shortenUrl(any(ShortenUrlRequest.class));
    }
}