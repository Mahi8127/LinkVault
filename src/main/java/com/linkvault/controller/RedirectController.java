package com.linkvault.controller;

import com.linkvault.model.ShortUrl;
import com.linkvault.service.ShortUrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.Optional;

/**
 * Controller handling public short URL redirects.
 */
@Controller
public class RedirectController {

    private final ShortUrlService shortUrlService;

    public RedirectController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    /**
     * GET /{shortCode}
     * Looks up the short code, increments click count, and redirects to the original destination.
     * Matches 6-character alphanumeric short codes, preventing interference with static resources.
     */
    @GetMapping("/{shortCode:[a-zA-Z0-9]{6}}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<ShortUrl> optionalShortUrl = shortUrlService.getAndIncrementClick(shortCode);

        if (optionalShortUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("<html><body style='font-family:sans-serif;text-align:center;padding:50px;background:#0f172a;color:#f8fafc;'>" +
                            "<h1>404 - Link Not Found</h1>" +
                            "<p>The requested short link does not exist or has expired.</p>" +
                            "<a href='/' style='color:#38bdf8;'>Back to LinkVault</a>" +
                            "</body></html>");
        }

        String destination = optionalShortUrl.get().getOriginalUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destination))
                .build();
    }
}
