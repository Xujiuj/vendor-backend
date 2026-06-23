package org.dromara.carbon.vendor.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.domain.CvVendorTableField;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor table field definition business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvVendorTableField.class, reverseConvertGenerate = false)
public class CvVendorTableFieldBo extends BaseEntity {

    @NotNull(message = "字段定义ID不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "表分组不能为空", groups = { AddGroup.class, EditGroup.class })
    private String tableGroup;

    @NotBlank(message = "表编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String tableCode;

    @NotBlank(message = "字段编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fieldKey;

    @NotBlank(message = "字段名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fieldLabel;

    @NotBlank(message = "字段类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fieldType;

    private Integer fieldPrecision;

    private Integer fieldWidth;

    private Boolean requiredFlag;

    private Integer sortOrder;

    private String status;

    private String remark;
}
