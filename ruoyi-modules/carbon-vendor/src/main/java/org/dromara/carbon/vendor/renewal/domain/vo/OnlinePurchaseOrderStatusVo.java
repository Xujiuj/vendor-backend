package org.dromara.carbon.vendor.renewal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Public order status response — sensitive fields (payUrl, issuedLicenseId) excluded.
 */
@Data
public class OnlinePurchaseOrderStatusVo {

    private String orderNo;
    private Long packageId;
    private String packageName;
    private String payChannel;
    private BigDecimal amount;
    private String currency;
    private String orderStatus;
    private String issueStatus;

}
