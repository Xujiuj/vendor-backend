package org.dromara.carbon.vendor.template.domain;

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
     * Package identifier controlling this scope.
     */
    private Long packageId;

    /**
     * Package name snapshot.
     */
    private String packageName;

    /**
     * License identifier.
     */
    private String licenseId;

    /**
     * License edition/package scope.
     */
    private String edition;

    /**
     * Scope lifecycle status.
     */
    private String scopeStatus;

    /**
     * Creation time.
     */
    private Date createTime;
}
