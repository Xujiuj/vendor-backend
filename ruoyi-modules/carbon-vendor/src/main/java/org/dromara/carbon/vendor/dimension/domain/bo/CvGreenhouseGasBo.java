package org.dromara.carbon.vendor.dimension.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvGreenhouseGas;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 温室气体维度业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvGreenhouseGas.class, reverseConvertGenerate = false)
public class CvGreenhouseGasBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 气体编码
     */
    @NotBlank(message = "气体编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String gasCode;

    /**
     * 气体名称（中文）
     */
    @NotBlank(message = "气体名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String gasName;

    /**
     * 气体名称（英文）
     */
    private String gasNameEn;

    /**
     * 全球变暖潜能值（GWP）
     */
    private BigDecimal gwpValue;

    /**
     * GWP版本（如AR5、AR6）
     */
    private String gwpVersion;

    /**
     * 化学式
     */
    private String chemicalFormula;

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
