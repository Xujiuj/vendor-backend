package org.dromara.carbon.vendor.factor.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
public class CvFactorCustomerScopeBo extends BaseEntity {

    @NotNull(message = "id cannot be null", groups = EditGroup.class)
    private Long id;

    @NotNull(message = "versionId cannot be null", groups = AddGroup.class)
    private Long versionId;

    private Long packageId;
    private String packageName;
    private String scopeStatus;
}
