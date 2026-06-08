package org.dromara.carbon.vendor.domain.license;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * Service-level request for audited license revocation.
 */
@Data
public class CvLicenseRevokeRequest {

    /**
     * Issued license identifier to revoke.
     */
    @NotBlank(message = "licenseId cannot be blank")
    private String licenseId;

    /**
     * Operator that revoked the license.
     */
    private String revokedBy;

    /**
     * Revocation timestamp.
     */
    private Date revokedAt;

    /**
     * Revocation audit reason.
     */
    private String revokeReason;
}
