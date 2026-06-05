package org.dromara.carbon.vendor.domain.license;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Service-level request for audited revoked-license reissue.
 *
 * <p>Install ID stays bound to the revoked source license unless
 * {@code allowInstallIdChange} is true and {@code targetInstallId} is provided.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CvLicenseReissueRequest extends CvLicenseIssueRequest {

    /**
     * Revoked source license identifier to reissue from.
     */
    private String sourceLicenseId;

    /**
     * Explicit approval to move the reissued license to a new installId.
     */
    private Boolean allowInstallIdChange;

    /**
     * Replacement installId used only when allowInstallIdChange is true.
     */
    private String targetInstallId;
}
