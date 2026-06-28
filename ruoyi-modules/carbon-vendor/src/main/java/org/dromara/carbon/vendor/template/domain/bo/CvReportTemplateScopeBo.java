package org.dromara.carbon.vendor.template.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateScope;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor report template scope business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvReportTemplateScope.class, reverseConvertGenerate = false)
public class CvReportTemplateScopeBo extends BaseEntity {

     /**
     * Primary key.
     */
    @NotNull(message = "id cannot be null", groups = { EditGroup.class })
    private Long id;

     /**
     * Report template identifier.
     */
    @NotNull(message = "templateId cannot be null", groups = { AddGroup.class, EditGroup.class })
    private Long templateId;

    /**
     * Package identifier controlling this scope.
     */
    private Long packageId;

    /**
     * Package name snapshot.
     */
    private String packageName;

    /**
     * Scope lifecycle status.
     */
    private String scopeStatus;
}
