package org.dromara.carbon.vendor.domain.bo.dimension;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactor;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 电力排放因子业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvElectricityFactor.class, reverseConvertGenerate = false)
public class CvElectricityFactorBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 因子版本
     */
    @NotBlank(message = "因子版本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String factorVersion;

    /**
     * 行政区划编码
     */
    @NotBlank(message = "行政区划编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String divisionCode;

    /**
     * 行政区划名称
     */
    private String divisionName;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 省级电网排放因子
     */
    private BigDecimal provinceFactor;

    /**
     * 区域电网排放因子
     */
    private BigDecimal regionFactor;

    /**
     * 全国电网排放因子
     */
    private BigDecimal nationalFactor;

    /**
     * 非化石能源电力排放因子
     */
    private BigDecimal nonFossilExcludedFactor;

    /**
     * 全国化石能源电力排放因子
     */
    private BigDecimal nationalFossilPowerFactor;

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
