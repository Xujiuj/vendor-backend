package org.dromara.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Public open-api protection settings.
 */
@Data
@ConfigurationProperties(prefix = "open-api.protection")
public class OpenApiProtectionProperties {

    private Boolean enabled = true;

    private String pathPattern = "/open/*";

    private RateLimit rateLimit = new RateLimit();

    private Signature signature = new Signature();

    @Data
    public static class RateLimit {

        private Boolean enabled = true;

        private Integer time = 60;

        private Integer count = 300;

        private Integer idleTimeout = 120;

    }

    @Data
    public static class Signature {

        private Boolean enabled = false;

        private Integer timestampWindowSeconds = 300;

        private String appKeyHeader = "X-App-Key";

        private String timestampHeader = "X-Timestamp";

        private String nonceHeader = "X-Nonce";

        private String signatureHeader = "X-Signature";

        private String contentSha256Header = "X-Content-SHA256";

        private Boolean requireContentSha256 = false;

        private Map<String, String> secrets = new HashMap<>();

    }

}
