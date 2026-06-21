package org.dromara.system.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.system.domain.SysTenantPackage;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 租户套餐业务对象 sys_tenant_package
 *
 * @author Michelle.Chung
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysTenantPackage.class, reverseConvertGenerate = false)
public class SysTenantPackageBo extends BaseEntity {

    /**
     * 租户套餐id
     */
    @NotNull(message = "租户套餐id不能为空", groups = { EditGroup.class })
    private Long packageId;

    /**
     * 套餐名称
     */
    @NotBlank(message = "套餐名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String packageName;

    /**
     * 关联菜单id
     */
    @AutoMapping(target = "menuIds", expression = "java(org.dromara.common.core.utils.StringUtils.joinComma(source.getMenuIds()))")
    private Long[] menuIds;

    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单树选择项是否关联显示
     */
    private Boolean menuCheckStrictly;

    /**
     * 套餐价格金额
     */
    @DecimalMin(value = "0.00", message = "套餐价格金额不能小于0", groups = { AddGroup.class, EditGroup.class })
    @Digits(integer = 10, fraction = 2, message = "套餐价格金额最多10位整数和2位小数", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal priceAmount;

    /**
     * 价格币种
     */
    @Size(max = 3, message = "价格币种长度不能超过3个字符", groups = { AddGroup.class, EditGroup.class })
    private String priceCurrency;

    /**
     * 计费周期
     */
    @Size(max = 20, message = "计费周期长度不能超过20个字符", groups = { AddGroup.class, EditGroup.class })
    private String billingCycle;

    /**
     * 是否允许在线购买
     */
    private Boolean onlinePurchaseEnabled;

    /**
     * Whether paid online orders should issue licenses automatically.
     */
    private Boolean licenseAutoIssueEnabled;

    /**
     * Signing key id used for licenses issued from this package.
     */
    @Size(max = 64, message = "License keyId length cannot exceed 64", groups = { AddGroup.class, EditGroup.class })
    private String licenseKeyId;

    /**
     * License validity days for online purchase issue.
     */
    @Min(value = 1, message = "License validity days must be greater than 0", groups = { AddGroup.class, EditGroup.class })
    private Integer licenseValidityDays;

    /**
     * Comma-separated license feature codes.
     */
    private String licenseFeatureCodes;

    /**
     * JSON array of template entitlements in license.v1 format.
     */
    private String licenseTemplateEntitlements;

    /**
     * 状态（0正常 1停用）
     */
    private String status;


}
