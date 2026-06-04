package org.dromara.carbon.vendor.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * Vendor factor record business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvFactorRecord.class, reverseConvertGenerate = false)
public class CvFactorRecordBo extends BaseEntity {

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
     * Stable factor code.
     */
    @NotBlank(message = "factorCode cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String factorCode;

     /**
     * Factor display name.
     */
    @NotBlank(message = "factorName cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String factorName;

     /**
     * Factor category.
     */
    @NotBlank(message = "factorCategory cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String factorCategory;

     /**
     * Factor value.
     */
    @NotNull(message = "factorValue cannot be null", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal factorValue;

     /**
     * Factor unit.
     */
    @NotBlank(message = "factorUnit cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String factorUnit;

    /**
     * Source reference.
     */
    private String sourceRef;

    /**
     * Whether this factor is enabled.
     */
    private Boolean enabledFlag;
}
