package org.dromara.carbon.vendor.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.domain.CvFactorCustomerScope;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor factor customer scope business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvFactorCustomerScope.class, reverseConvertGenerate = false)
public class CvFactorCustomerScopeBo extends BaseEntity {

    /**
     * Primary key.
     */
    @NotNull(message = "id cannot be null", groups = { EditGroup.class })
    private Long id;

    /**
     * Factor version identifier.
     */
    @NotNull(message = "versionId cannot be null", groups = { AddGroup.class, EditGroup.class })
    private Long versionId;

    /**
     * Customer identifier.
     */
    private Long customerId;

    /**
     * Edition code.
     */
    private String edition;

    /**
     * License identifier.
     */
    private String licenseId;

    /**
     * Scope lifecycle status.
     */
    private String scopeStatus;
}
