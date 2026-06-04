package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor license issue view object.
 */
@Data
@AutoMapper(target = CvLicenseIssue.class)
public class CvLicenseIssueVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    private Long id;

    /**
     * Issued license identifier.
     */
    private String licenseId;

    /**
     * Customer identifier.
     */
    private Long customerId;

    /**
     * Key identifier.
     */
    private String keyId;

    /**
     * Signature algorithm.
     */
    private String algorithm;

    /**
     * License schema version.
     */
    private String schemaVersion;

    /**
     * Edition code.
     */
    private String edition;

    /**
     * Enabled feature codes.
     */
    private String featureCodes;

    /**
     * Installation identifier.
     */
    private String installId;

    /**
     * License validity start time.
     */
    private Date validFrom;

    /**
     * License validity end time.
     */
    private Date validTo;

    /**
     * Issue lifecycle status.
     */
    private String issueStatus;

    /**
     * Issue type.
     */
    private String issueType;

    /**
     * Operator that issued the license.
     */
    private String issuedBy;

    /**
     * Issue timestamp.
     */
    private Date issuedTime;

    /**
     * Revocation timestamp.
     */
    private Date revokedTime;

    /**
     * Serialized license payload.
     */
    private String licensePayload;

    /**
     * Signature text.
     */
    private String signatureText;
}
