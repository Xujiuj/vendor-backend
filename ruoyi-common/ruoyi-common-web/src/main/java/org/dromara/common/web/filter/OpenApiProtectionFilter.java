package org.dromara.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.web.config.properties.OpenApiProtectionProperties;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Protects anonymous public APIs before requests reach controllers.
 */
public class OpenApiProtectionFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_KEY_PREFIX = "open-api:rate:";

    private static final String NONCE_KEY_PREFIX = "open-api:nonce:";

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final OpenApiProtectionProperties properties;

    private final OpenApiProtectionStore store;

    public OpenApiProtectionFilter(OpenApiProtectionProperties properties, OpenApiProtectionStore store) {
        this.properties = properties;
        this.store = store;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (Boolean.FALSE.equals(properties.getEnabled())) {
            filterChain.doFilter(request, response);
            return;
        }
        String appKey = getHeader(request, properties.getSignature().getAppKeyHeader());
        if (!checkRateLimit(request, response, appKey)) {
            return;
        }
        if (Boolean.TRUE.equals(properties.getSignature().getEnabled()) && !checkSignature(request, response, appKey)) {
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkRateLimit(HttpServletRequest request, HttpServletResponse response, String appKey) {
        OpenApiProtectionProperties.RateLimit rateLimit = properties.getRateLimit();
        if (Boolean.FALSE.equals(rateLimit.getEnabled())) {
            return true;
        }
        int time = positive(rateLimit.getTime(), 60);
        int count = positive(rateLimit.getCount(), 300);
        int idleTimeout = Math.max(positive(rateLimit.getIdleTimeout(), time * 2), time);
        String identity = StringUtils.isNotBlank(appKey) ? appKey : ServletUtils.getClientIP(request);
        String key = RATE_LIMIT_KEY_PREFIX + normalizeKey(request.getRequestURI()) + ":" + normalizeKey(identity);
        if (!store.tryAcquire(key, count, time, idleTimeout)) {
            render(response, 429, "Too many requests");
            return false;
        }
        return true;
    }

    private boolean checkSignature(HttpServletRequest request, HttpServletResponse response, String appKey) {
        OpenApiProtectionProperties.Signature signature = properties.getSignature();
        String timestamp = getHeader(request, signature.getTimestampHeader());
        String nonce = getHeader(request, signature.getNonceHeader());
        String requestSignature = getHeader(request, signature.getSignatureHeader());
        if (StringUtils.isAnyBlank(appKey, timestamp, nonce, requestSignature)) {
            render(response, 401, "Missing open-api signature headers");
            return false;
        }
        String secret = signature.getSecrets().get(appKey);
        if (StringUtils.isBlank(secret)) {
            render(response, 401, "Unknown open-api app key");
            return false;
        }
        int timestampWindowSeconds = positive(signature.getTimestampWindowSeconds(), 300);
        if (!isFreshTimestamp(timestamp, timestampWindowSeconds)) {
            render(response, 401, "Expired open-api timestamp");
            return false;
        }
        if (Boolean.TRUE.equals(signature.getRequireContentSha256())
            && StringUtils.isBlank(getHeader(request, signature.getContentSha256Header()))) {
            render(response, 401, "Missing open-api content hash");
            return false;
        }
        String expected = hmacSha256Hex(secret, canonicalPayload(request, timestamp, nonce));
        if (!constantTimeEquals(expected, requestSignature)) {
            render(response, 401, "Invalid open-api signature");
            return false;
        }
        String nonceKey = NONCE_KEY_PREFIX + normalizeKey(appKey) + ":" + normalizeKey(nonce);
        if (!store.putNonceIfAbsent(nonceKey, Duration.ofSeconds(timestampWindowSeconds))) {
            render(response, 401, "Repeated open-api nonce");
            return false;
        }
        return true;
    }

    private String canonicalPayload(HttpServletRequest request, String timestamp, String nonce) {
        return request.getMethod().toUpperCase() + "\n"
            + request.getRequestURI() + "\n"
            + canonicalQuery(request) + "\n"
            + timestamp + "\n"
            + nonce + "\n"
            + getHeader(request, properties.getSignature().getContentSha256Header());
    }

    private String canonicalQuery(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> Arrays.stream(entry.getValue())
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .map(value -> entry.getKey() + "=" + value)
                .collect(Collectors.joining("&")))
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining("&"));
    }

    private boolean isFreshTimestamp(String timestamp, int timestampWindowSeconds) {
        try {
            long requestTime = Long.parseLong(timestamp);
            if (timestamp.length() <= 10) {
                requestTime *= 1000;
            }
            long diff = Math.abs(System.currentTimeMillis() - requestTime);
            return diff <= Duration.ofSeconds(timestampWindowSeconds).toMillis();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign open-api payload", e);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            actual.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String getHeader(HttpServletRequest request, String name) {
        return ServletUtils.getHeader(request, name).trim();
    }

    private int positive(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }

    private String normalizeKey(String value) {
        if (StringUtils.isBlank(value)) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9:_./-]", "_");
    }

    private void render(HttpServletResponse response, int code, String message) {
        ServletUtils.renderString(response, "{\"code\":" + code + ",\"msg\":\"" + escapeJson(message) + "\",\"data\":null}");
        response.setStatus(code);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
