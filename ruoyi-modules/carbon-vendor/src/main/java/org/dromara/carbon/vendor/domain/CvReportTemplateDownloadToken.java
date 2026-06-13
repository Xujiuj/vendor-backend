package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report template one-time download token.
 */
@Data
@TableName("cv_report_template_download_token")
public class CvReportTemplateDownloadToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String downloadToken;

    private String licenseId;

    private String installId;

    private Long customerId;

    private Long templateId;

    private String fileName;

    private String fileUri;

    private String tokenStatus;

    private Date expiresTime;

    private Date consumedTime;

    private Date createTime;
}
