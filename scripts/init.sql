-- URL Shortener Database Initialization Script

-- Create database if not exists (for local development)
CREATE DATABASE IF NOT EXISTS urlshortener;
USE urlshortener;

-- Create url_mapping table
CREATE TABLE IF NOT EXISTS url_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    short_url VARCHAR(10) NOT NULL UNIQUE,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    click_count BIGINT DEFAULT 0,
    INDEX idx_short_url (short_url),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at)
);

-- Insert sample data for testing
INSERT INTO url_mapping (short_url, long_url, click_count) VALUES
('github', 'https://github.com', 100),
('google', 'https://www.google.com', 250),
('spring', 'https://spring.io', 75),
('k8s', 'https://kubernetes.io', 150),
('docker', 'https://www.docker.com', 200);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_long_url_hash ON url_mapping (long_url(255));
CREATE INDEX IF NOT EXISTS idx_click_count ON url_mapping (click_count DESC);

-- Create a view for statistics
CREATE OR REPLACE VIEW url_stats AS
SELECT 
    COUNT(*) as total_urls,
    SUM(click_count) as total_clicks,
    AVG(click_count) as avg_clicks_per_url,
    MAX(click_count) as max_clicks,
    COUNT(CASE WHEN expires_at IS NOT NULL AND expires_at < NOW() THEN 1 END) as expired_urls,
    COUNT(CASE WHEN DATE(created_at) = CURDATE() THEN 1 END) as urls_created_today
FROM url_mapping;

-- Create a procedure for cleanup
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS CleanupExpiredUrls()
BEGIN
    DELETE FROM url_mapping 
    WHERE expires_at IS NOT NULL 
    AND expires_at < NOW();
    
    SELECT ROW_COUNT() as deleted_count;
END //
DELIMITER ;

-- Grant permissions (for local development user)
GRANT ALL PRIVILEGES ON urlshortener.* TO 'urluser'@'%';
FLUSH PRIVILEGES;