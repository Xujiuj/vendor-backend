package org.dromara.carbon.vendor.dimension.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 行政区划表 cv_admin_division
 *
 * @author carbon
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cv_admin_division")
public class CvAdminDivision extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
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
     * 层级类型（province/city/district）
     */
    private String levelType;

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
