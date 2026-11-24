package com.linkvault.service;

import com.linkvault.model.ShortUrl;
import com.linkvault.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        shortUrlService = new ShortUrlServiceImpl(shortUrlRepository);
    }

    @Test
    void shortenUrl_validUrl_generatesSixCharCodeAndSaves() {
        when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrl result = shortUrlService.shortenUrl("https://example.com/very/long/path");

        assertNotNull(result);
        assertEquals("https://example.com/very/long/path", result.getOriginalUrl());
        assertNotNull(result.getShortCode());
        assertEquals(6, result.getShortCode().length());
        assertEquals(0, result.getClickCount());
        assertNotNull(result.getCreatedAt());

        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    void shortenUrl_invalidUrl_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> shortUrlService.shortenUrl(""));
        assertThrows(IllegalArgumentException.class, () -> shortUrlService.shortenUrl("ftp://invalid.com"));
        assertThrows(IllegalArgumentException.class, () -> shortUrlService.shortenUrl("just-a-string"));
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void getAndIncrementClick_existingCode_incrementsAndSaves() {
        ShortUrl existing = new ShortUrl("https://example.com", "abc123");
        existing.setClickCount(2);

        when(shortUrlRepository.findByShortCode("abc123")).thenReturn(Optional.of(existing));
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ShortUrl> result = shortUrlService.getAndIncrementClick("abc123");

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getClickCount());
        verify(shortUrlRepository).save(existing);
    }

    @Test
    void getAndIncrementClick_nonExistingCode_returnsEmpty() {
        when(shortUrlRepository.findByShortCode("nonexist")).thenReturn(Optional.empty());

        Optional<ShortUrl> result = shortUrlService.getAndIncrementClick("nonexist");

        assertTrue(result.isEmpty());
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void getAllUrls_returnsListFromRepository() {
        List<ShortUrl> sampleList = List.of(
                new ShortUrl("https://a.com", "codeA1"),
                new ShortUrl("https://b.com", "codeB2")
        );
        when(shortUrlRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(sampleList);

        List<ShortUrl> result = shortUrlService.getAllUrls();

        assertEquals(2, result.size());
        verify(shortUrlRepository).findAll(any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void deleteUrl_existingId_deletesAndReturnsTrue() {
        when(shortUrlRepository.existsById("existing-id")).thenReturn(true);

        boolean result = shortUrlService.deleteUrl("existing-id");

        assertTrue(result);
        verify(shortUrlRepository, times(1)).deleteById("existing-id");
    }

    @Test
    void deleteUrl_nonExistingId_returnsFalse() {
        when(shortUrlRepository.existsById("missing-id")).thenReturn(false);

        boolean result = shortUrlService.deleteUrl("missing-id");

        assertFalse(result);
        verify(shortUrlRepository, never()).deleteById(anyString());
    }
}
