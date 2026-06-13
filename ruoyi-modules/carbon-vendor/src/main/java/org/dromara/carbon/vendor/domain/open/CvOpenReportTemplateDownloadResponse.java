package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * License-scoped open report template download metadata.
 */
@Data
public class CvOpenReportTemplateDownloadResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private Long templateId;

    private String templateCode;

    private String templateName;

    private String templateVersion;

    private String fileName;

    private String fileUri;

    private String downloadToken;

    private Date downloadTokenExpiresTime;

    private Date publishedTime;
}
