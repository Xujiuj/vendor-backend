package org.dromara.carbon.vendor.dimension.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 电力因子版本视图对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvElectricityFactorVersion.class)
public class CvElectricityFactorVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 因子版本号
     */
    private String factorVersion;

    /**
     * 生效年份
     */
    private Integer effectiveYear;

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
