package org.dromara.carbon.vendor.domain.license;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Service-level request for manual license issue.
 */
@Data
public class CvLicenseIssueRequest {

    private Long customerId;

    private String customerCode;

    private String customerName;

    private String keyId;

    private String schemaVersion;

    private String algorithm;

    private Long packageId;

    private String packageName;

    private String edition;

    private List<String> features;

    private String installId;

    private Date validFrom;

    private Date validTo;

    private Date issuedAt;

    private String issuedBy;

    private String issueType;

    private String licenseId;

    private List<CvTemplateEntitlement> templateEntitlements;
}
