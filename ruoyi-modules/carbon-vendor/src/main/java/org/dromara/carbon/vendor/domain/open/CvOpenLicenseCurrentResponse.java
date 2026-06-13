package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * License-scoped current license response for enterprise backend.
 */
@Data
public class CvOpenLicenseCurrentResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private Long customerId;

    private String status;

    private String edition;

    private String featureCodes;

    private String keyId;

    private String algorithm;

    private String schemaVersion;

    private Date validFrom;

    private Date validTo;

    private String licensePayload;

    private String signatureText;
}
