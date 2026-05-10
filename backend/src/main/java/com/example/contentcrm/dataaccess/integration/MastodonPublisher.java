package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MastodonPublisher implements PlatformPublisher {
    private static final String MEDIA_MANUAL_FALLBACK_MESSAGE = "Mastodon media posts require manual publication in this MVP; "
            + "text-only Mastodon posts are published automatically.";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public MastodonPublisher(ObjectMapper objectMapper) {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build(), objectMapper);
    }

    MastodonPublisher(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Platform platform() {
        return Platform.MASTODON;
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        if (!StringUtils.hasText(request.instanceUrl()) || !StringUtils.hasText(request.accessToken())) {
            return PublishResult.failure("Mastodon instance URL and access token are required", "{\"error\":\"mastodon-settings-missing\"}");
        }
        if (hasMedia(request.mediaPaths())) {
            return PublishResult.failure(MEDIA_MANUAL_FALLBACK_MESSAGE, "{\"error\":\"mastodon-media-manual-fallback\"}");
        }

        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("status", request.text() == null ? "" : request.text());
            params.put("visibility", "public");

            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(request.instanceUrl().trim() + "/api/v1/statuses"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + request.accessToken().trim())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Idempotency-Key", "dmposts-mastodon-variant-" + request.variantId())
                    .POST(HttpRequest.BodyPublishers.ofString(formEncode(params)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode json = readMastodonJson(response);
            String externalPostId = json.path("id").asText(null);
            if (!StringUtils.hasText(externalPostId)) {
                return PublishResult.failure("Mastodon API error: post id is missing", json.toString());
            }
            String externalPostUrl = json.path("url").asText(null);
            if (!StringUtils.hasText(externalPostUrl)) {
                externalPostUrl = json.path("uri").asText(null);
            }
            return PublishResult.success(externalPostId, externalPostUrl, json.toString());
        } catch (MastodonApiException e) {
            return PublishResult.failure(e.getMessage(), e.responsePayload());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PublishResult.failure("Mastodon publication failed: request interrupted", "{\"error\":\"interrupted\"}");
        } catch (Exception e) {
            return PublishResult.failure("Mastodon publication failed: " + e.getMessage(), "{\"error\":\"mastodon-request-failed\"}");
        }
    }

    private boolean hasMedia(List<String> mediaPaths) {
        return mediaPaths != null && mediaPaths.stream().anyMatch(StringUtils::hasText);
    }

    private JsonNode readMastodonJson(HttpResponse<String> response) throws IOException {
        String body = response.body() == null ? "" : response.body();
        JsonNode json = StringUtils.hasText(body) ? objectMapper.readTree(body) : objectMapper.createObjectNode();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = json.path("error").asText("");
            if (StringUtils.hasText(message)) {
                throw new MastodonApiException("Mastodon HTTP error " + response.statusCode() + ": " + message, body);
            }
            throw new MastodonApiException("Mastodon HTTP error " + response.statusCode(), body);
        }
        String message = json.path("error").asText("");
        if (StringUtils.hasText(message)) {
            throw new MastodonApiException("Mastodon API error: " + message, body);
        }
        return json;
    }

    private String formEncode(Map<String, String> params) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            parts.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return String.join("&", parts);
    }

    private static class MastodonApiException extends RuntimeException {
        private final String responsePayload;

        MastodonApiException(String message, String responsePayload) {
            super(message);
            this.responsePayload = responsePayload;
        }

        String responsePayload() {
            return responsePayload;
        }
    }
}
