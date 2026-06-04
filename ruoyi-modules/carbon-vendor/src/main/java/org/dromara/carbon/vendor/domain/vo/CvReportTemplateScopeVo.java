package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvReportTemplateScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report template scope view object.
 */
@Data
@AutoMapper(target = CvReportTemplateScope.class)
public class CvReportTemplateScopeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
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
