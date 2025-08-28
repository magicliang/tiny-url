package com.shorturl.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shorturl.UrlShortenerApplication;
import com.shorturl.dto.ShortenUrlRequest;
import com.shorturl.dto.ShortenUrlResponse;
import com.shorturl.entity.UrlMapping;
import com.shorturl.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for URL Shortener service
 */
@SpringBootTest(classes = UrlShortenerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        urlMappingRepository.deleteAll();
    }

    @Test
    void testFullWorkflow_ShortenAndExpand() throws Exception {
        // Step 1: Shorten URL
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.example.com");
        
        MvcResult shortenResult = mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longUrl").value("https://www.example.com"))
                .andReturn();

        String responseJson = shortenResult.getResponse().getContentAsString();
        ShortenUrlResponse response = objectMapper.readValue(responseJson, ShortenUrlResponse.class);
        
        // Extract short URL from full URL
        String fullShortUrl = response.getShortUrl();
        String shortUrlCode = fullShortUrl.substring(fullShortUrl.lastIndexOf("/") + 1);

        // Verify URL was saved in database
        Optional<UrlMapping> savedMapping = urlMappingRepository.findByShortUrl(shortUrlCode);
        assertTrue(savedMapping.isPresent());
        assertEquals("https://www.example.com", savedMapping.get().getLongUrl());

        // Step 2: Expand URL (redirect)
        mockMvc.perform(get("/api/v1/" + shortUrlCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://www.example.com"));

        // Verify click count was incremented
        Optional<UrlMapping> updatedMapping = urlMappingRepository.findByShortUrl(shortUrlCode);
        assertTrue(updatedMapping.isPresent());
        assertEquals(1L, updatedMapping.get().getClickCount());

        // Step 3: Get statistics
        mockMvc.perform(get("/api/v1/stats/" + shortUrlCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(shortUrlCode))
                .andExpect(jsonPath("$.longUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.clickCount").value(1))
                .andExpect(jsonPath("$.expired").value(false));
    }

    @Test
    void testCustomAlias() throws Exception {
        // Create URL with custom alias
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.google.com");
        request.setCustomAlias("google");

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longUrl").value("https://www.google.com"));

        // Verify custom alias works
        mockMvc.perform(get("/api/v1/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://www.google.com"));

        // Verify in database
        Optional<UrlMapping> mapping = urlMappingRepository.findByShortUrl("google");
        assertTrue(mapping.isPresent());
        assertEquals("https://www.google.com", mapping.get().getLongUrl());
    }

    @Test
    void testDuplicateCustomAlias() throws Exception {
        // Create first URL with custom alias
        ShortenUrlRequest request1 = new ShortenUrlRequest("https://www.example.com");
        request1.setCustomAlias("test");

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Try to create second URL with same custom alias
        ShortenUrlRequest request2 = new ShortenUrlRequest("https://www.google.com");
        request2.setCustomAlias("test");

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testExpiredUrl() throws Exception {
        // Create URL mapping that's already expired
        UrlMapping expiredMapping = new UrlMapping("expired", "https://www.expired.com");
        expiredMapping.setExpiresAt(LocalDateTime.now().minusDays(1));
        urlMappingRepository.save(expiredMapping);

        // Try to access expired URL
        mockMvc.perform(get("/api/v1/expired"))
                .andExpect(status().isNotFound());

        // Verify stats show it's expired
        mockMvc.perform(get("/api/v1/stats/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expired").value(true));
    }

    @Test
    void testAdminEndpoints() throws Exception {
        // Create some test data
        UrlMapping mapping1 = new UrlMapping("test1", "https://www.test1.com");
        UrlMapping mapping2 = new UrlMapping("test2", "https://www.test2.com");
        mapping2.setClickCount(5L);
        
        urlMappingRepository.save(mapping1);
        urlMappingRepository.save(mapping2);

        // Test get all URLs
        mockMvc.perform(get("/api/v1/admin/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // Test system stats
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUrls").value(2))
                .andExpect(jsonPath("$.totalClicks").value(5));

        // Test delete URL
        mockMvc.perform(delete("/api/v1/admin/urls/test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("URL deleted successfully"));

        // Verify URL was deleted
        assertFalse(urlMappingRepository.findByShortUrl("test1").isPresent());
        assertTrue(urlMappingRepository.findByShortUrl("test2").isPresent());
    }

    @Test
    void testCleanupExpiredUrls() throws Exception {
        // Create expired and non-expired URLs
        UrlMapping expiredMapping = new UrlMapping("expired1", "https://www.expired1.com");
        expiredMapping.setExpiresAt(LocalDateTime.now().minusDays(1));
        
        UrlMapping validMapping = new UrlMapping("valid1", "https://www.valid1.com");
        validMapping.setExpiresAt(LocalDateTime.now().plusDays(1));
        
        urlMappingRepository.save(expiredMapping);
        urlMappingRepository.save(validMapping);

        // Run cleanup
        mockMvc.perform(post("/api/v1/admin/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(1));

        // Verify only expired URL was deleted
        assertFalse(urlMappingRepository.findByShortUrl("expired1").isPresent());
        assertTrue(urlMappingRepository.findByShortUrl("valid1").isPresent());
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("URL Shortener"));
    }

    @Test
    void testInvalidRequests() throws Exception {
        // Test invalid URL format
        ShortenUrlRequest invalidRequest = new ShortenUrlRequest("not-a-url");
        
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test empty URL
        ShortenUrlRequest emptyRequest = new ShortenUrlRequest("");
        
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());

        // Test non-existent short URL
        mockMvc.perform(get("/api/v1/nonexistent"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/stats/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testExistingLongUrl() throws Exception {
        // Create URL first time
        ShortenUrlRequest request = new ShortenUrlRequest("https://www.duplicate.com");
        
        MvcResult result1 = mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ShortenUrlResponse response1 = objectMapper.readValue(
                result1.getResponse().getContentAsString(), ShortenUrlResponse.class);

        // Create same URL again
        MvcResult result2 = mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ShortenUrlResponse response2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(), ShortenUrlResponse.class);

        // Should return the same short URL
        assertEquals(response1.getShortUrl(), response2.getShortUrl());
        
        // Should only have one entry in database
        assertEquals(1, urlMappingRepository.count());
    }
}