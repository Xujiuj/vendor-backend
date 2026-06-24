package org.dromara.carbon.vendor.domain.bo.dimension;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactorVersion;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 电力因子版本业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvElectricityFactorVersion.class, reverseConvertGenerate = false)
public class CvElectricityFactorVersionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 因子版本号
     */
    @NotBlank(message = "因子版本号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String factorVersion;

    /**
     * 生效年份
     */
    @NotNull(message = "生效年份不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer effectiveYear;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
