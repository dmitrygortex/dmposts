package com.example.contentcrm.dataaccess.integration;

import com.example.contentcrm.business.model.enums.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MastodonPublisherTest {

    @Test
    void publishesTextStatusToConfiguredInstance() {
        CapturingHttpClient httpClient = new CapturingHttpClient(200, "{\"id\":\"109\",\"url\":\"https://mastodon.example/@owner/109\"}");
        MastodonPublisher publisher = new MastodonPublisher(httpClient, new ObjectMapper());

        PublishResult result = publisher.publish(new PublishRequest(
                42L,
                Platform.MASTODON,
                "Mastodon text",
                List.of(),
                "mastodon-token",
                null,
                null,
                "https://mastodon.example"
        ));

        assertThat(result.success()).isTrue();
        assertThat(result.externalPostId()).isEqualTo("109");
        assertThat(result.externalPostUrl()).isEqualTo("https://mastodon.example/@owner/109");
        assertThat(httpClient.request.method()).isEqualTo("POST");
        assertThat(httpClient.request.uri()).isEqualTo(URI.create("https://mastodon.example/api/v1/statuses"));
        assertThat(httpClient.request.headers().firstValue("Authorization")).contains("Bearer mastodon-token");
        assertThat(httpClient.request.headers().firstValue("Idempotency-Key")).contains("dmposts-mastodon-variant-42");
        assertThat(httpClient.requestBody).contains("status=Mastodon+text");
        assertThat(httpClient.requestBody).contains("visibility=public");
    }

    @Test
    void returnsFailureForMissingTokenOrInstanceUrl() {
        CapturingHttpClient httpClient = new CapturingHttpClient(200, "{\"id\":\"unused\"}");
        MastodonPublisher publisher = new MastodonPublisher(httpClient, new ObjectMapper());

        PublishResult result = publisher.publish(new PublishRequest(
                43L,
                Platform.MASTODON,
                "Mastodon text",
                List.of(),
                "",
                null,
                null,
                ""
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Mastodon instance URL and access token are required");
        assertThat(httpClient.request).isNull();
    }

    @Test
    void returnsManualFallbackForMediaPostWithoutCallingApi() {
        CapturingHttpClient httpClient = new CapturingHttpClient(200, "{\"id\":\"unused\"}");
        MastodonPublisher publisher = new MastodonPublisher(httpClient, new ObjectMapper());

        PublishResult result = publisher.publish(new PublishRequest(
                44L,
                Platform.MASTODON,
                "Mastodon text with image",
                List.of("/tmp/image.jpg"),
                "mastodon-token",
                null,
                null,
                "https://mastodon.example"
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage())
                .isEqualTo("Mastodon media posts require manual publication in this MVP; text-only Mastodon posts are published automatically.");
        assertThat(httpClient.request).isNull();
    }

    @Test
    void returnsFailureForMastodonApiError() {
        CapturingHttpClient httpClient = new CapturingHttpClient(401, "{\"error\":\"The access token is invalid\"}");
        MastodonPublisher publisher = new MastodonPublisher(httpClient, new ObjectMapper());

        PublishResult result = publisher.publish(new PublishRequest(
                45L,
                Platform.MASTODON,
                "Mastodon text",
                List.of(),
                "bad-token",
                null,
                null,
                "https://mastodon.example"
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Mastodon HTTP error 401: The access token is invalid");
    }

    private static class CapturingHttpClient extends HttpClient {
        private final int statusCode;
        private final String responseBody;
        private HttpRequest request;
        private String requestBody;

        CapturingHttpClient(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            this.request = request;
            this.requestBody = readBody(request);
            @SuppressWarnings("unchecked")
            T body = (T) responseBody;
            return new FakeHttpResponse<>(request, statusCode, body);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        }

        private String readBody(HttpRequest request) {
            return request.bodyPublisher()
                    .map(this::readBodyPublisher)
                    .orElse("");
        }

        private String readBodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            CountDownLatch complete = new CountDownLatch(1);
            AtomicReference<Throwable> error = new AtomicReference<>();
            bodyPublisher.subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    byte[] bytes = new byte[item.remaining()];
                    item.get(bytes);
                    try {
                        output.write(bytes);
                    } catch (IOException e) {
                        error.set(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                    complete.countDown();
                }

                @Override
                public void onComplete() {
                    complete.countDown();
                }
            });
            try {
                complete.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            if (error.get() != null) {
                throw new IllegalStateException(error.get());
            }
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private record FakeHttpResponse<T>(HttpRequest request, int statusCode, T body) implements HttpResponse<T> {
        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
