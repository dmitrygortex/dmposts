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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class VkPublisher implements PlatformPublisher {
    private static final String DEFAULT_API_BASE_URL = "https://api.vk.com";
    private static final String DEFAULT_API_VERSION = "5.199";
    private static final String IMAGE_MANUAL_FALLBACK_MESSAGE = "VK image posts require manual publication in this MVP; "
            + "text-only VK posts are published automatically.";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;

    @Autowired
    public VkPublisher(ObjectMapper objectMapper) {
        this(HttpClient.newHttpClient(), objectMapper, DEFAULT_API_BASE_URL);
    }

    VkPublisher(HttpClient httpClient, ObjectMapper objectMapper, String apiBaseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public Platform platform() {
        return Platform.VK;
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        if (!StringUtils.hasText(request.accessToken()) || !StringUtils.hasText(request.communityId())) {
            return PublishResult.failure("VK token and community id are required", "{\"error\":\"vk-settings-missing\"}");
        }

        String groupId = normalizeGroupId(request.communityId());
        if (!StringUtils.hasText(groupId)) {
            return PublishResult.failure("VK token and community id are required", "{\"error\":\"vk-settings-missing\"}");
        }

        String accessToken = request.accessToken().trim();
        String apiVersion = StringUtils.hasText(request.apiVersion()) ? request.apiVersion().trim() : DEFAULT_API_VERSION;
        String ownerId = "-" + groupId;

        try {
            if (hasMedia(request.mediaPaths())) {
                return PublishResult.failure(IMAGE_MANUAL_FALLBACK_MESSAGE, "{\"error\":\"vk-image-manual-fallback\"}");
            }
            Map<String, String> params = new LinkedHashMap<>();
            params.put("owner_id", ownerId);
            params.put("from_group", "1");
            params.put("message", request.text() == null ? "" : request.text());

            JsonNode response = callVkApi("wall.post", params, accessToken, apiVersion);
            JsonNode postIdNode = response.path("response").path("post_id");
            if (!postIdNode.canConvertToInt()) {
                return PublishResult.failure("VK API error: post id is missing", response.toString());
            }

            String externalPostId = ownerId + "_" + postIdNode.asInt();
            return PublishResult.success(
                    externalPostId,
                    "https://vk.com/wall" + externalPostId,
                    response.toString()
            );
        } catch (VkApiException e) {
            return PublishResult.failure(e.getMessage(), e.responsePayload());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PublishResult.failure("VK publication failed: request interrupted", "{\"error\":\"interrupted\"}");
        } catch (Exception e) {
            return PublishResult.failure("VK publication failed: " + e.getMessage(), "{\"error\":\"vk-request-failed\"}");
        }
    }

    private boolean hasMedia(List<String> mediaPaths) {
        return mediaPaths != null && mediaPaths.stream().anyMatch(StringUtils::hasText);
    }

    private JsonNode callVkApi(String method, Map<String, String> params, String accessToken, String apiVersion) throws IOException, InterruptedException {
        Map<String, String> fullParams = new LinkedHashMap<>(params);
        fullParams.put("access_token", accessToken);
        fullParams.put("v", apiVersion);

        HttpRequest request = HttpRequest.newBuilder(URI.create(apiBaseUrl + "/method/" + method))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formEncode(fullParams)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return readVkJson(response);
    }

    private JsonNode readVkJson(HttpResponse<String> response) throws IOException {
        String body = response.body() == null ? "" : response.body();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new VkApiException("VK HTTP error " + response.statusCode(), body);
        }

        JsonNode json = objectMapper.readTree(body);
        JsonNode error = json.path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            int code = error.path("error_code").asInt();
            String message = error.path("error_msg").asText("Unknown error");
            if (code == 27 && message.toLowerCase().contains("group authorization failed")) {
                message = "Group authorization failed. VK rejected this token for the requested method. "
                        + "Image posts are handled by manual fallback in this MVP; text-only posts use the configured token when VK allows it.";
            }
            throw new VkApiException("VK API error " + code + ": " + message, json.toString());
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

    private String normalizeGroupId(String communityId) {
        String value = communityId.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        int queryStart = value.indexOf('?');
        if (queryStart >= 0) {
            value = value.substring(0, queryStart);
        }
        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0) {
            value = value.substring(lastSlash + 1);
        }
        return value
                .replaceFirst("^-", "")
                .replaceFirst("^(club|public|group)", "");
    }

    private static class VkApiException extends RuntimeException {
        private final String responsePayload;

        VkApiException(String message, String responsePayload) {
            super(message);
            this.responsePayload = responsePayload;
        }

        String responsePayload() {
            return responsePayload;
        }
    }
}
