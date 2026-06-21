package org.dromara.carbon.vendor.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Normalized payment notification payload for tests and gateway adapters.
 */
@Data
public class PaymentNotifyBo {

    private String orderNo;
    private String payChannel;
    private String tradeNo;
    private BigDecimal amount;
    private String currency;
    private String tradeStatus;
    private String signature;
    private String rawBody;

}
