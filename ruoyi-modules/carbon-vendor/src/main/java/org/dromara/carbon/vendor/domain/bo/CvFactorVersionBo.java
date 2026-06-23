package org.dromara.carbon.vendor.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Vendor factor version query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvFactorVersion.class, reverseConvertGenerate = false)
public class CvFactorVersionBo extends BaseEntity {

    /**
     * Primary key.
     */
    @NotNull(message = "版本ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * Stable version code.
     */
    @NotBlank(message = "版本编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String versionCode;

    /**
     * Version display name.
     */
    @NotBlank(message = "版本名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String versionName;

    /**
     * Publish lifecycle status.
     */
    private String publishStatus;

    /**
     * Whether this version is frozen.
     */
    private Boolean frozenFlag;

    /**
     * Operator that published the version.
     */
    private String publishedBy;
}
