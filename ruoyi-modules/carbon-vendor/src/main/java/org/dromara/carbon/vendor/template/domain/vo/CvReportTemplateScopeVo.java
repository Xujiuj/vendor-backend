package org.dromara.carbon.vendor.template.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateScope;

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
