package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VkPublisherTest {
    private HttpServer server;
    private String apiBaseUrl;
    private final List<String> requests = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        apiBaseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void publishesTextPostToConfiguredCommunity() {
        respondJson("/method/wall.post", "{\"response\":{\"post_id\":789}}");
        VkPublisher publisher = new VkPublisher(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl);

        PublishResult result = publisher.publish(new PublishRequest(
                10L,
                Platform.VK,
                "VK text",
                List.of(),
                "vk-token",
                "123",
                "5.199",
                null
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.externalPostId()).isEqualTo("-123_789");
        assertThat(result.externalPostUrl()).isEqualTo("https://vk.com/wall-123_789");
        assertThat(requests).singleElement().satisfies(request -> {
            assertThat(request).contains("POST /method/wall.post");
            assertThat(request).contains("owner_id=-123");
            assertThat(request).contains("from_group=1");
            assertThat(request).contains("message=VK+text");
            assertThat(request).contains("access_token=vk-token");
            assertThat(request).contains("v=5.199");
        });
    }

    @Test
    void acceptsCommunityUrlAsGroupId() {
        respondJson("/method/wall.post", "{\"response\":{\"post_id\":789}}");
        VkPublisher publisher = new VkPublisher(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl);

        PublishResult result = publisher.publish(new PublishRequest(
                10L,
                Platform.VK,
                "VK text",
                List.of(),
                "vk-token",
                "https://vk.com/club238241783",
                "5.199",
                null
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.externalPostId()).isEqualTo("-238241783_789");
        assertThat(requests).singleElement().satisfies(request -> {
            assertThat(request).contains("owner_id=-238241783");
            assertThat(request).doesNotContain("club238241783");
        });
    }

    @Test
    void returnsManualFallbackForImagePostWithoutCallingVkApi() throws IOException {
        Path image = Files.createTempFile("vk-wall-photo", ".jpg");
        Files.write(image, new byte[]{1, 2, 3});
        VkPublisher publisher = new VkPublisher(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl);

        PublishResult result = publisher.publish(new PublishRequest(
                11L,
                Platform.VK,
                "Text with image",
                List.of(image.toString()),
                "vk-token",
                "123",
                "5.199",
                null
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage())
                .isEqualTo("VK image posts require manual publication in this MVP; text-only VK posts are published automatically.");
        assertThat(requests).isEmpty();
    }

    @Test
    void returnsFailureForVkApiError() {
        respondJson("/method/wall.post", "{\"error\":{\"error_code\":5,\"error_msg\":\"User authorization failed\"}}");
        VkPublisher publisher = new VkPublisher(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl);

        PublishResult result = publisher.publish(new PublishRequest(
                12L,
                Platform.VK,
                "VK text",
                List.of(),
                "bad-token",
                "123",
                "5.199",
                null
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("VK API error 5: User authorization failed");
    }

    @Test
    void explainsGroupTokenAuthorizationError() {
        respondJson("/method/wall.post", "{\"error\":{\"error_code\":27,\"error_msg\":\"Group authorization failed: method is unavailable with group auth.\"}}");
        VkPublisher publisher = new VkPublisher(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl);

        PublishResult result = publisher.publish(new PublishRequest(
                12L,
                Platform.VK,
                "VK text",
                List.of(),
                "group-token",
                "123",
                "5.199",
                null
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage())
                .isEqualTo("VK API error 27: Group authorization failed. VK rejected this token for the requested method. Image posts are handled by manual fallback in this MVP; text-only posts use the configured token when VK allows it.");
    }

    @Test
    void returnsFailureWhenTokenOrCommunityMissing() {
        VkPublisher publisher = new VkPublisher(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl);

        PublishResult result = publisher.publish(new PublishRequest(
                13L,
                Platform.VK,
                "VK text",
                List.of(),
                "",
                "",
                "5.199",
                null
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("VK token and community id are required");
    }

    private void respondJson(String path, String response) {
        server.createContext(path, exchange -> {
            requests.add(formatRequest(exchange));
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    private String formatRequest(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        return exchange.getRequestMethod() + " " + uri.getPath() + " " + (uri.getQuery() == null ? "" : uri.getQuery()) + "\n"
                + (contentType == null ? "" : contentType) + "\n"
                + body;
    }
}
