package org.dromara.carbon.vendor.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Public order creation request from an enterprise-local deployment.
 */
@Data
public class OnlinePurchaseCreateBo {

    @NotNull(message = "套餐不能为空")
    private Long packageId;

    @NotBlank(message = "支付渠道不能为空")
    private String payChannel;

    @NotBlank(message = "企业名称不能为空")
    private String customerName;

    private String customerCode;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String licenseId;
    private String installId;
    private String idempotencyKey;
    private String returnUrl;

}
