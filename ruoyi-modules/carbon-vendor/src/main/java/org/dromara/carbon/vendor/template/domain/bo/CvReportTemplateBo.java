package org.dromara.carbon.vendor.template.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.template.domain.CvReportTemplate;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * Vendor report template business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvReportTemplate.class, reverseConvertGenerate = false)
public class CvReportTemplateBo extends BaseEntity {

     /**
     * Primary key.
     */
    @NotNull(message = "id cannot be null", groups = { EditGroup.class })
    private Long id;

     /**
     * Stable template code.
     */
    @NotBlank(message = "templateCode cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String templateCode;

     /**
     * Template display name.
     */
    @NotBlank(message = "templateName cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String templateName;

     /**
     * Template version.
     */
    @NotBlank(message = "templateVersion cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String templateVersion;

     /**
     * Stored file name.
     */
    @NotBlank(message = "fileName cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String fileName;

     /**
     * Stored file URI.
     */
    @NotBlank(message = "fileUri cannot be blank", groups = { AddGroup.class, EditGroup.class })
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
     * Remark.
     */
    private String remark;
}
