package com.shorturl.repository;

import com.shorturl.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UrlMapping entity
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Find URL mapping by short URL
     */
    Optional<UrlMapping> findByShortUrl(String shortUrl);

    /**
     * Find URL mapping by long URL
     */
    Optional<UrlMapping> findByLongUrl(String longUrl);

    /**
     * Check if short URL exists
     */
    boolean existsByShortUrl(String shortUrl);

    /**
     * Find all expired URLs
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    List<UrlMapping> findExpiredUrls(@Param("now") LocalDateTime now);

    /**
     * Delete expired URLs
     */
    @Modifying
    @Query("DELETE FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    int deleteExpiredUrls(@Param("now") LocalDateTime now);

    /**
     * Increment click count for a short URL
     */
    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortUrl = :shortUrl")
    int incrementClickCount(@Param("shortUrl") String shortUrl);

    /**
     * Find URLs created after a specific date
     */
    List<UrlMapping> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find top URLs by click count
     */
    @Query("SELECT u FROM UrlMapping u ORDER BY u.clickCount DESC")
    List<UrlMapping> findTopUrlsByClickCount();

    /**
     * Count total URLs
     */
    @Query("SELECT COUNT(u) FROM UrlMapping u")
    long countTotalUrls();

    /**
     * Count URLs created today
     */
    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE DATE(u.createdAt) = CURRENT_DATE")
    long countUrlsCreatedToday();

    /**
     * Get total click count
     */
    @Query("SELECT SUM(u.clickCount) FROM UrlMapping u")
    Long getTotalClickCount();
}