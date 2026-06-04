package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report template customer scope cv_report_template_scope.
 */
@Data
@TableName("cv_report_template_scope")
public class CvReportTemplateScope implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
    private Long id;

    /**
     * Report template identifier.
     */
    private Long templateId;

    /**
     * Customer identifier.
     */
    private Long customerId;

    /**
     * License identifier.
     */
    private String licenseId;

    /**
     * Scope lifecycle status.
     */
    private String scopeStatus;

    /**
     * Creation time.
     */
    private Date createTime;
}
