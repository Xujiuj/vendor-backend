package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report template version cv_report_template.
 */
@Data
@TableName("cv_report_template")
public class CvReportTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
    private Long id;

    /**
     * Stable template code.
     */
    private String templateCode;

    /**
     * Template display name.
     */
    private String templateName;

    /**
     * Template version.
     */
    private String templateVersion;

    /**
     * Stored file name.
     */
    private String fileName;

    /**
     * Stored file URI.
     */
    private String fileUri;

    /**
     * Publish lifecycle status.
     */
    private String publishStatus;

    /**
     * Operator that published the template.
     */
    private String publishedBy;

    /**
     * Publish timestamp.
     */
    private Date publishedTime;

    /**
     * Creation time.
     */
    private Date createTime;

    /**
     * Remark.
     */
    private String remark;
}
