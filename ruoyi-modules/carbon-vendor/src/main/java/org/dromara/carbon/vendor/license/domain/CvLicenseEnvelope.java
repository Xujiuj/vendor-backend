package org.dromara.carbon.vendor.license.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Versioned license file envelope.
 */
@Data
@JsonPropertyOrder({"schemaVersion", "algorithm", "keyId", "payload", "signature"})
public class CvLicenseEnvelope {

    private String schemaVersion;

    private String algorithm;

    private String keyId;

    private CvLicensePayload payload;

    private String signature;
}
