package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.math.BigDecimal;

import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 租户套餐对象 sys_tenant_package
 *
 * @author Michelle.Chung
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant_package")
public class SysTenantPackage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户套餐id
     */
    @TableId(value = "package_id")
    private Long packageId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 关联菜单id
     */
    private String menuIds;

    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    private Boolean menuCheckStrictly;

    /**
     * 套餐价格金额
     */
    private BigDecimal priceAmount;

    /**
     * 价格币种
     */
    private String priceCurrency;

    /**
     * 计费周期
     */
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
    private String licenseKeyId;

    /**
     * License validity days for online purchase issue.
     */
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

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

}
