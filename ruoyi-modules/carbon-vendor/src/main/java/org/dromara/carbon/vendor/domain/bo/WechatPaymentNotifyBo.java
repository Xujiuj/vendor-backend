package org.dromara.carbon.vendor.domain.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * WeChat Pay API v3 payment notification payload.
 */
@Data
public class WechatPaymentNotifyBo {

    private String id;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("resource_type")
    private String resourceType;

    @JsonProperty("event_type")
    private String eventType;

    private String summary;

    private Resource resource;

    @Data
    public static class Resource {
        private String algorithm;
        private String ciphertext;

        @JsonProperty("associated_data")
        private String associatedData;

        private String nonce;

        @JsonProperty("original_type")
        private String originalType;
    }
}
