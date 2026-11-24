package com.linkvault.controller;

import com.linkvault.dto.ShortenRequest;
import com.linkvault.dto.ShortenResponse;
import com.linkvault.model.ShortUrl;
import com.linkvault.service.ShortUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for URL shortening and management operations.
 */
@RestController
@RequestMapping("/api/urls")
public class UrlApiController {

    private final ShortUrlService shortUrlService;

    public UrlApiController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    /**
     * POST /api/urls/shorten
     * Shortens a provided original URL and returns metadata with the public short URL.
     */
    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody ShortenRequest request) {
        if (request == null || request.getUrl() == null || request.getUrl().isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "The 'url' field is required and cannot be empty.");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            ShortUrl saved = shortUrlService.shortenUrl(request.getUrl());
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            ShortenResponse response = toResponse(saved, baseUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * GET /api/urls
     * Returns all shortened URLs and their click counts.
     */
    @GetMapping
    public ResponseEntity<List<ShortenResponse>> getAllUrls() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        List<ShortenResponse> responses = shortUrlService.getAllUrls().stream()
                .map(shortUrl -> toResponse(shortUrl, baseUrl))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * DELETE /api/urls/{id}
     * Deletes a shortened URL by its unique MongoDB identifier.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String id) {
        boolean deleted = shortUrlService.deleteUrl(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ShortenResponse toResponse(ShortUrl shortUrl, String baseUrl) {
        return new ShortenResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                baseUrl + "/" + shortUrl.getShortCode(),
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt()
        );
    }
}
