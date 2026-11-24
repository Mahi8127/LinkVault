package com.linkvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.dto.ShortenRequest;
import com.linkvault.model.ShortUrl;
import com.linkvault.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UrlApiController.class, RedirectController.class})
class UrlApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void shortenUrl_validUrl_returns201Created() throws Exception {
        ShortUrl mockSaved = new ShortUrl("https://example.com/test", "xyz123");
        mockSaved.setId("mock-id-123");

        when(shortUrlService.shortenUrl("https://example.com/test")).thenReturn(mockSaved);

        ShortenRequest request = new ShortenRequest("https://example.com/test");

        mockMvc.perform(post("/api/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("xyz123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/test"))
                .andExpect(jsonPath("$.shortUrl").value(containsString("/xyz123")))
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    void shortenUrl_emptyUrl_returns400BadRequest() throws Exception {
        ShortenRequest request = new ShortenRequest("");

        mockMvc.perform(post("/api/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAllUrls_returnsList() throws Exception {
        ShortUrl url1 = new ShortUrl("https://one.com", "code01");
        ShortUrl url2 = new ShortUrl("https://two.com", "code02");
        when(shortUrlService.getAllUrls()).thenReturn(List.of(url1, url2));

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].shortCode").value("code01"))
                .andExpect(jsonPath("$[1].shortCode").value("code02"));
    }

    @Test
    void redirect_existingShortCode_returns302WithLocation() throws Exception {
        ShortUrl mockShortUrl = new ShortUrl("https://destination.com/target", "abc999");
        when(shortUrlService.getAndIncrementClick("abc999")).thenReturn(Optional.of(mockShortUrl));

        mockMvc.perform(get("/abc999"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://destination.com/target"));
    }

    @Test
    void redirect_nonExistingShortCode_returns404() throws Exception {
        when(shortUrlService.getAndIncrementClick("notfnd")).thenReturn(Optional.empty());

        mockMvc.perform(get("/notfnd"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("404 - Link Not Found")));
    }

    @Test
    void deleteUrl_existingId_returns204NoContent() throws Exception {
        when(shortUrlService.deleteUrl("valid-id")).thenReturn(true);

        mockMvc.perform(delete("/api/urls/valid-id"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUrl_nonExistingId_returns404NotFound() throws Exception {
        when(shortUrlService.deleteUrl("missing-id")).thenReturn(false);

        mockMvc.perform(delete("/api/urls/missing-id"))
                .andExpect(status().isNotFound());
    }
}
