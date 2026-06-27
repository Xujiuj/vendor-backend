package org.dromara.carbon.vendor.template.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.template.domain.CvReportTemplate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report template view object.
 */
@Data
@AutoMapper(target = CvReportTemplate.class)
public class CvReportTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
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
