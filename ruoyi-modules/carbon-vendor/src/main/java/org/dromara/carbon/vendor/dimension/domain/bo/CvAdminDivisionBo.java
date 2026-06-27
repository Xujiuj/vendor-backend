package org.dromara.carbon.vendor.dimension.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvAdminDivision;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 行政区划业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvAdminDivision.class, reverseConvertGenerate = false)
public class CvAdminDivisionBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 行政区划编码
     */
    @NotBlank(message = "行政区划编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String divisionCode;

    /**
     * 行政区划名称
     */
    @NotBlank(message = "行政区划名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String divisionName;

    /**
     * 父级编码
     */
    private String parentCode;

    /**
     * 层级类型（1省 2市 3区县）
     */
    private Integer levelType;

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
