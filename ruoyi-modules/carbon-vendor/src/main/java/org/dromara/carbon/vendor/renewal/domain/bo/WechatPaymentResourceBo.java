package org.dromara.carbon.vendor.renewal.domain.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Decrypted WeChat Pay transaction resource.
 */
@Data
public class WechatPaymentResourceBo {

    @JsonProperty("appid")
    private String appId;

    @JsonProperty("mchid")
    private String merchantId;

    @JsonProperty("out_trade_no")
    private String outTradeNo;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("trade_state")
    private String tradeState;

    private Amount amount;

    @Data
    public static class Amount {
        private Integer total;
        private String currency;

        public BigDecimal totalYuan() {
            return total == null ? null : BigDecimal.valueOf(total, 2);
        }
    }
}
