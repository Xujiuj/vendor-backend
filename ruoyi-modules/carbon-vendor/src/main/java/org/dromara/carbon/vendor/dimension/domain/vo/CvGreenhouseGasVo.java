package org.dromara.carbon.vendor.dimension.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvGreenhouseGas;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 温室气体维度视图对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvGreenhouseGas.class)
public class CvGreenhouseGasVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
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
