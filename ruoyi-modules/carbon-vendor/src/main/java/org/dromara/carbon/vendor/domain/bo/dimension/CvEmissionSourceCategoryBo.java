package org.dromara.carbon.vendor.domain.bo.dimension;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.carbon.vendor.domain.dimension.CvEmissionSourceCategory;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * 排放源分类业务对象
 *
 * @author carbon
 */
@Data
@AutoMapper(target = CvEmissionSourceCategory.class, reverseConvertGenerate = false)
public class CvEmissionSourceCategoryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String categoryCode;

    /**
     * 分类名称（中文）
     */
    @NotBlank(message = "分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
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
