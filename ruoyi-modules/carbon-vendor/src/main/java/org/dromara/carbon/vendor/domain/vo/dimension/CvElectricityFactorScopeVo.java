package org.dromara.carbon.vendor.domain.vo.dimension;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactorScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 电力因子口径维度视图对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvElectricityFactorScope.class)
public class CvElectricityFactorScopeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 范围标识键
     */
    private String scopeKey;

    /**
     * 范围名称
     */
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
