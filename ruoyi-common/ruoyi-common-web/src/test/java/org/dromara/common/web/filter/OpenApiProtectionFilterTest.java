package org.dromara.common.web.filter;

import jakarta.servlet.ServletException;
import org.dromara.common.web.config.properties.OpenApiProtectionProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class OpenApiProtectionFilterTest {

    @Test
    void allowsRequestsWithinRateLimit() throws Exception {
        OpenApiProtectionProperties properties = properties();
        properties.getRateLimit().setCount(2);
        InMemoryStore store = new InMemoryStore();
        OpenApiProtectionFilter filter = new OpenApiProtectionFilter(properties, store);

        MockHttpServletResponse first = invoke(filter, request("/open/factors"), new MockFilterChain());
        MockHttpServletResponse second = invoke(filter, request("/open/factors"), new MockFilterChain());

        assertEquals(200, first.getStatus());
        assertEquals("", first.getContentAsString());
        assertEquals(200, second.getStatus());
        assertEquals("", second.getContentAsString());
    }

    @Test
    void blocksRequestsAboveRateLimit() throws Exception {
        OpenApiProtectionProperties properties = properties();
        properties.getRateLimit().setCount(1);
        InMemoryStore store = new InMemoryStore();
        OpenApiProtectionFilter filter = new OpenApiProtectionFilter(properties, store);

        invoke(filter, request("/open/factors"), new MockFilterChain());
        MockHttpServletResponse blocked = invoke(filter, request("/open/factors"), new MockFilterChain());

        assertEquals(429, blocked.getStatus());
        assertTrue(blocked.getContentAsString().contains("\"code\":429"));
        assertTrue(blocked.getContentAsString().contains("Too many requests"));
    }

    @Test
    void acceptsValidSignatureAndRejectsNonceReplay() throws Exception {
        OpenApiProtectionProperties properties = signedProperties();
        InMemoryStore store = new InMemoryStore();
        OpenApiProtectionFilter filter = new OpenApiProtectionFilter(properties, store);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "nonce-1";
        MockHttpServletRequest request = signedRequest("/open/factors", "currentVersionCode=FV-1", timestamp, nonce, "secret");

        MockHttpServletResponse accepted = invoke(filter, request, new MockFilterChain());
        MockHttpServletResponse replay = invoke(
            filter,
            signedRequest("/open/factors", "currentVersionCode=FV-1", timestamp, nonce, "secret"),
            new MockFilterChain()
        );

        assertEquals("", accepted.getContentAsString());
        assertEquals(401, replay.getStatus());
        assertTrue(replay.getContentAsString().contains("\"code\":401"));
        assertTrue(replay.getContentAsString().contains("Repeated open-api nonce"));
    }

    @Test
    void rejectsInvalidSignatureWithoutConsumingNonce() throws Exception {
        OpenApiProtectionProperties properties = signedProperties();
        InMemoryStore store = new InMemoryStore();
        OpenApiProtectionFilter filter = new OpenApiProtectionFilter(properties, store);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "nonce-2";
        MockHttpServletRequest invalid = signedRequest("/open/factors", "", timestamp, nonce, "wrong-secret");

        MockHttpServletResponse rejected = invoke(filter, invalid, new MockFilterChain());
        MockHttpServletResponse accepted = invoke(
            filter,
            signedRequest("/open/factors", "", timestamp, nonce, "secret"),
            new MockFilterChain()
        );

        assertEquals(401, rejected.getStatus());
        assertTrue(rejected.getContentAsString().contains("Invalid open-api signature"));
        assertEquals("", accepted.getContentAsString());
    }

    @Test
    void enforcesConcurrentRateLimit() throws Exception {
        OpenApiProtectionProperties properties = properties();
        properties.getRateLimit().setCount(10);
        properties.getRateLimit().setTime(60);
        InMemoryStore store = new InMemoryStore();
        OpenApiProtectionFilter filter = new OpenApiProtectionFilter(properties, store);
        int requests = 80;
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger blocked = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < requests; i++) {
                futures.add(executor.submit(() -> {
                    try {
                        start.await(2, TimeUnit.SECONDS);
                        MockHttpServletResponse response = invoke(filter, request("/open/factors"), new MockFilterChain());
                        if (response.getContentAsString().contains("\"code\":429")) {
                            blocked.incrementAndGet();
                        } else {
                            accepted.incrementAndGet();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }

        assertEquals(10, accepted.get());
        assertEquals(70, blocked.get());
    }

    private MockHttpServletResponse invoke(OpenApiProtectionFilter filter, MockHttpServletRequest request, MockFilterChain chain)
        throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr("10.0.0.8");
        return request;
    }

    private MockHttpServletRequest signedRequest(
        String uri, String query, String timestamp, String nonce, String secret) {
        MockHttpServletRequest request = request(uri);
        request.setQueryString(query);
        if (!query.isEmpty()) {
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                request.addParameter(parts[0], parts.length == 2 ? parts[1] : "");
            }
        }
        request.addHeader("X-App-Key", "app-1");
        request.addHeader("X-Timestamp", timestamp);
        request.addHeader("X-Nonce", nonce);
        request.addHeader("X-Signature", signature(secret, "GET\n" + uri + "\n" + query + "\n" + timestamp + "\n" + nonce + "\n"));
        return request;
    }

    private OpenApiProtectionProperties properties() {
        OpenApiProtectionProperties properties = new OpenApiProtectionProperties();
        properties.getRateLimit().setTime(60);
        properties.getRateLimit().setIdleTimeout(120);
        return properties;
    }

    private OpenApiProtectionProperties signedProperties() {
        OpenApiProtectionProperties properties = properties();
        properties.getSignature().setEnabled(true);
        properties.getSignature().setSecrets(Map.of("app-1", "secret"));
        return properties;
    }

    private String signature(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class InMemoryStore implements OpenApiProtectionStore {

        private final Map<String, AtomicInteger> permits = new ConcurrentHashMap<>();

        private final Map<String, Long> nonces = new ConcurrentHashMap<>();

        @Override
        public boolean tryAcquire(String key, int count, int time, int idleTimeout) {
            return permits.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet() <= count;
        }

        @Override
        public boolean putNonceIfAbsent(String key, Duration duration) {
            long expiresAt = System.nanoTime() + duration.toNanos();
            Long existing = nonces.putIfAbsent(key, expiresAt);
            return existing == null || existing < System.nanoTime() && nonces.replace(key, existing, expiresAt);
        }

    }

}
