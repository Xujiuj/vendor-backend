package org.dromara.carbon.vendor.dimension.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorScope;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 电力因子口径维度业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvElectricityFactorScope.class, reverseConvertGenerate = false)
public class CvElectricityFactorScopeBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 范围标识键
     */
    @NotBlank(message = "范围标识键不能为空", groups = { AddGroup.class, EditGroup.class })
    private String scopeKey;

    /**
     * 范围名称
     */
    @NotBlank(message = "范围名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String scopeName;

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
