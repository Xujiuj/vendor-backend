package org.dromara.carbon.vendor.renewal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Payment gateway settings. Merchant secrets stay on the online vendor side.
 */
@Data
@Component
@ConfigurationProperties(prefix = "carbon.vendor.payment")
public class VendorPaymentProperties {

    /**
     * Public vendor web origin used to build cashier URLs.
     */
    private String cashierBaseUrl = "https://www.carbondata.com/pay";

    /**
     * Require gateway signature verification before accepting callbacks.
     */
    private boolean requireSignature = true;

    private Channel wechat = new Channel();

    private Channel alipay = new Channel();

    @Data
    public static class Channel {
        private boolean enabled = true;
        private String appId;
        private String merchantId;
        private String notifyUrl;
        private String returnUrl;
        private String publicKey;
        private String apiV3Key;
        private String platformCertificate;
        private String sellerId;
    }

}
