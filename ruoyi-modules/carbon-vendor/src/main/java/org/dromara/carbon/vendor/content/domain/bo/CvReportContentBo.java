package org.dromara.carbon.vendor.content.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.content.domain.CvReportContent;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor report content catalog business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvReportContent.class, reverseConvertGenerate = false)
public class CvReportContentBo extends BaseEntity {

    @NotNull(message = "id cannot be null", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "directoryNo cannot be null", groups = { AddGroup.class, EditGroup.class })
    private Integer directoryNo;

    @NotBlank(message = "directoryName cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String directoryName;

    @NotNull(message = "subdirectoryNo cannot be null", groups = { AddGroup.class, EditGroup.class })
    private Integer subdirectoryNo;

    @NotBlank(message = "subdirectoryName cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String subdirectoryName;

    @NotBlank(message = "chartNames cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String chartNames;

    private Integer displayOrder;

    private String status;

    private String remark;
}
