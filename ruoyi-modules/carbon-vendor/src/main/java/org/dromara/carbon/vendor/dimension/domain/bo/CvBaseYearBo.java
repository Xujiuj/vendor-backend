package org.dromara.carbon.vendor.dimension.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvBaseYear;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基准年业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvBaseYear.class, reverseConvertGenerate = false)
public class CvBaseYearBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 基准年Key
     */
    @NotBlank(message = "基准年Key不能为空", groups = { AddGroup.class, EditGroup.class })
    private String baseYearKey;

    /**
     * 基准年份
     */
    @NotNull(message = "基准年份不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer baseYear;

    /**
     * 是否当前基准年（0否 1是）
     */
    private Integer isCurrent;

    /**
     * 说明
     */
    private String description;

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
