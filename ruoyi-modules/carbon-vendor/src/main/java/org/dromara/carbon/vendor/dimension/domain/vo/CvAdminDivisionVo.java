package org.dromara.carbon.vendor.dimension.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvAdminDivision;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 行政区划视图对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvAdminDivision.class)
public class CvAdminDivisionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 行政区划编码
     */
    private String divisionCode;

    /**
     * 行政区划名称
     */
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
