package org.dromara.carbon.vendor.dimension.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 温室气体表 cv_greenhouse_gas
 *
 * @author carbon
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cv_greenhouse_gas")
public class CvGreenhouseGas extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 气体编码
     */
    private String gasCode;

    /**
     * 气体名称（中文）
     */
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
