package org.dromara.carbon.vendor.dimension.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 电力排放因子表 cv_electricity_factor
 *
 * @author carbon
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cv_electricity_factor")
public class CvElectricityFactor extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
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
     * 备注
     */
    private String remark;

}
