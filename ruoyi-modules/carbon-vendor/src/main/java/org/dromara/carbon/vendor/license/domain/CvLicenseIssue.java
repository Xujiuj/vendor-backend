package org.dromara.carbon.vendor.license.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("cv_license_issue")
public class CvLicenseIssue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String licenseId;
    private Long customerId;
    private Long packageId;
    private String packageName;
    private String keyId;
    private String algorithm;
    private String schemaVersion;
    private String edition;
    private String featureCodes;
    private String installId;
    private Date validFrom;
    private Date validTo;
    private String issueStatus;
    private String issueType;
    private String sourceLicenseId;
    private String issuedBy;
    private Date issuedTime;
    private Date revokedTime;
    private String revokedBy;
    private String revokeReason;
    private String licensePayload;
    private String signatureText;
}
