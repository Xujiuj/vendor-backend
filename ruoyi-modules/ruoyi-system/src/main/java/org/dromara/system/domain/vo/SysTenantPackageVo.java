package org.dromara.system.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.system.domain.SysTenantPackage;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 租户套餐视图对象 sys_tenant_package
 *
 * @author Michelle.Chung
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysTenantPackage.class)
public class SysTenantPackageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户套餐id
     */
    @ExcelProperty(value = "租户套餐id")
    private Long packageId;

    /**
     * 套餐名称
     */
    @ExcelProperty(value = "套餐名称")
    private String packageName;

    /**
     * 关联菜单id
     */
    @ExcelProperty(value = "关联菜单id")
    private String menuIds;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 菜单树选择项是否关联显示
     */
    @ExcelProperty(value = "菜单树选择项是否关联显示")
    private Boolean menuCheckStrictly;

    /**
     * 套餐价格金额
     */
    @ExcelProperty(value = "套餐价格金额")
    private BigDecimal priceAmount;

    /**
     * 价格币种
     */
    @ExcelProperty(value = "价格币种")
    private String priceCurrency;

    /**
     * 计费周期
     */
    @ExcelProperty(value = "计费周期")
    private String billingCycle;

    /**
     * 是否允许在线购买
     */
    @ExcelProperty(value = "是否允许在线购买")
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
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=停用")
    private String status;


}
