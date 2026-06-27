package org.dromara.carbon.vendor.license.domain;

import lombok.Data;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;

/**
 * Result of vendor manual license issue.
 */
@Data
public class CvLicenseIssueResult {

    private boolean issued;

    private String status;

    private String message;

    private String licenseContent;

    private CvLicenseIssue licenseIssue;

    public static CvLicenseIssueResult issued(String licenseContent, CvLicenseIssue licenseIssue) {
        CvLicenseIssueResult result = new CvLicenseIssueResult();
        result.setIssued(true);
        result.setStatus("ISSUED");
        result.setMessage("license issued");
        result.setLicenseContent(licenseContent);
        result.setLicenseIssue(licenseIssue);
        return result;
    }

    public static CvLicenseIssueResult failed(String status, String message) {
        CvLicenseIssueResult result = new CvLicenseIssueResult();
        result.setIssued(false);
        result.setStatus(status);
        result.setMessage(message);
        return result;
    }
}
