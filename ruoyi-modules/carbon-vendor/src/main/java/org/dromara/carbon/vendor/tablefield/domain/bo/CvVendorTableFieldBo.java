package org.dromara.carbon.vendor.tablefield.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
public class CvVendorTableFieldBo extends BaseEntity {

    @NotNull(message = "id cannot be null", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "tableGroup cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String tableGroup;

    @NotBlank(message = "tableCode cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String tableCode;

    @NotBlank(message = "fieldKey cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String fieldKey;

    @NotBlank(message = "fieldLabel cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String fieldLabel;

    private String fieldType;
    private Integer fieldPrecision;
    private Integer fieldWidth;
    private String fieldOptions;
    private Boolean requiredFlag;
    private Integer sortOrder;
    private String status;
    private String remark;
}
