package org.dromara.carbon.vendor.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor license issue query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CvLicenseIssueBo extends BaseEntity {

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
     * Installation identifier.
     */
    private String installId;

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
}
