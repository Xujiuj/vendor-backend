package org.dromara.carbon.vendor.dimension.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 电力排放因子视图对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvElectricityFactor.class)
public class CvElectricityFactorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * Source(A) 202EF PK_因子版本省份代码.
     */
    private String versionProvinceCode;

    /**
     * 因子版本
     */
    private String factorVersion;

    /**
     * 行政区划编码
     */
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;
}
