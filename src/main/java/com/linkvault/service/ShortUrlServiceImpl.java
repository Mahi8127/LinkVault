package com.linkvault.service;

import com.linkvault.model.ShortUrl;
import com.linkvault.repository.ShortUrlRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of ShortUrlService.
 */
@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_COLLISION_RETRIES = 10;

    private final ShortUrlRepository shortUrlRepository;
    private final SecureRandom random = new SecureRandom();

    public ShortUrlServiceImpl(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Override
    public ShortUrl shortenUrl(String originalUrl) {
        String trimmedUrl = originalUrl.trim();
        validateUrl(trimmedUrl);

        String shortCode = generateUniqueShortCode();
        ShortUrl shortUrl = new ShortUrl(trimmedUrl, shortCode);
        return shortUrlRepository.save(shortUrl);
    }

    @Override
    public Optional<ShortUrl> getAndIncrementClick(String shortCode) {
        Optional<ShortUrl> optionalShortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (optionalShortUrl.isPresent()) {
            ShortUrl shortUrl = optionalShortUrl.get();
            shortUrl.setClickCount(shortUrl.getClickCount() + 1);
            shortUrlRepository.save(shortUrl);
            return Optional.of(shortUrl);
        }
        return Optional.empty();
    }

    @Override
    public List<ShortUrl> getAllUrls() {
        return shortUrlRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public boolean deleteUrl(String id) {
        if (shortUrlRepository.existsById(id)) {
            shortUrlRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_COLLISION_RETRIES; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                int index = random.nextInt(ALPHABET.length());
                sb.append(ALPHABET.charAt(index));
            }
            String candidateCode = sb.toString();
            if (!shortUrlRepository.existsByShortCode(candidateCode)) {
                return candidateCode;
            }
        }
        throw new IllegalStateException("Unable to generate a unique short code after multiple attempts. Please try again.");
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty.");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
        try {
            new java.net.URI(url).toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + e.getMessage());
        }
    }
}
