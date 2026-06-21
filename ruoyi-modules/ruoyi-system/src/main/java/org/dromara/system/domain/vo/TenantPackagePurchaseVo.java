package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.SysTenantPackage;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Online-purchasable tenant package view.
 */
@Data
@AutoMapper(target = SysTenantPackage.class)
public class TenantPackagePurchaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户套餐id
     */
    private Long packageId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 备注
     */
    private String remark;

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

}
