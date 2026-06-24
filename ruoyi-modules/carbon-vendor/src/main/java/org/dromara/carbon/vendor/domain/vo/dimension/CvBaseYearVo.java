package org.dromara.carbon.vendor.domain.vo.dimension;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.dimension.CvBaseYear;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 基准年视图对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvBaseYear.class)
public class CvBaseYearVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 工厂编码
     */
    private String factoryCode;

    /**
     * 工厂名称
     */
    private String factoryName;

    /**
     * 基准年份
     */
    private Integer baseYear;

    /**
     * 是否当前基准年（0否 1是）
     */
    private Integer isCurrent;

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
