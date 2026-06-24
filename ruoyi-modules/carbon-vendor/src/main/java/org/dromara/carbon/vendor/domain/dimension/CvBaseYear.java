package org.dromara.carbon.vendor.domain.dimension;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 基准年表 cv_base_year
 *
 * @author carbon
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cv_base_year")
public class CvBaseYear extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
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
     * 备注
     */
    private String remark;

}
