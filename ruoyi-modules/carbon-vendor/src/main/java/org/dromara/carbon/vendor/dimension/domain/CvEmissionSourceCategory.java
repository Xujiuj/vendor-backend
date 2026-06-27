package org.dromara.carbon.vendor.dimension.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 排放源分类表 cv_emission_source_category
 *
 * @author carbon
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cv_emission_source_category")
public class CvEmissionSourceCategory extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * Source(A)业务键
     */
    private String businessKey;

    /**
     * 分类名称（中文）
     */
    private String categoryName;

    /**
     * 分类名称（英文）
     */
    private String categoryNameEn;

    /**
     * 温室气体范围（Scope 1/2/3）
     */
    private String ghgScope;

    /**
     * 温室气体范围细分
     */
    private String ghgScopeCategory;

    /**
     * ISO分类
     */
    private String isoCategory;

    /**
     * ISO分类（英文）
     */
    private String isoCategoryEn;

    /**
     * ISO分类描述
     */
    private String isoCategoryDescription;

    /**
     * 国标范围分类
     */
    private String gbScopeCategory;

    /**
     * 国标子分类
     */
    private String gbSubcategory;

    /**
     * 父级编码
     */
    private String parentCode;

    /**
     * 生效日期
     */
    private java.time.LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private java.time.LocalDate expireDate;

    /**
     * 是否当前（Y/N）
     */
    private String currentFlag;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 统一标准分类
     */
    private String standardCategory;

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
