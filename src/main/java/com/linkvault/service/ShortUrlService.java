package com.linkvault.service;

import com.linkvault.model.ShortUrl;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for URL shortening and retrieval operations.
 */
public interface ShortUrlService {

    /**
     * Shortens a given URL and persists the document.
     */
    ShortUrl shortenUrl(String originalUrl);

    /**
     * Looks up a short URL by its 6-character code and increments its click count.
     */
    Optional<ShortUrl> getAndIncrementClick(String shortCode);

    /**
     * Retrieves all saved short URLs.
     */
    List<ShortUrl> getAllUrls();

    /**
     * Deletes a short URL record by its MongoDB document ID.
     * @return true if deleted, false if record does not exist.
     */
    boolean deleteUrl(String id);
}
