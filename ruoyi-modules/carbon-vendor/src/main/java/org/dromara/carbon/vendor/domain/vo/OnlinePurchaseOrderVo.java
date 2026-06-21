package org.dromara.carbon.vendor.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Public order response used by enterprise local deployments.
 */
@Data
public class OnlinePurchaseOrderVo {

    private String orderNo;
    private Long packageId;
    private String packageName;
    private String payChannel;
    private BigDecimal amount;
    private String currency;
    private String orderStatus;
    private String issueStatus;
    private String payUrl;
    private String payForm;
    private String issuedLicenseId;

}
