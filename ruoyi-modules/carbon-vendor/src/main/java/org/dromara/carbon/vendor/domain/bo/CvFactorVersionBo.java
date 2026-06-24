package org.dromara.carbon.vendor.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
public class CvFactorVersionBo extends BaseEntity {

    @NotNull(message = "id cannot be null", groups = EditGroup.class)
    private Long id;

    @NotBlank(message = "versionCode cannot be blank", groups = AddGroup.class)
    private String versionCode;

    @NotBlank(message = "versionName cannot be blank", groups = AddGroup.class)
    private String versionName;

    private String publishStatus;
    private Boolean frozenFlag;
    private String publishedBy;
    private String remark;
}
