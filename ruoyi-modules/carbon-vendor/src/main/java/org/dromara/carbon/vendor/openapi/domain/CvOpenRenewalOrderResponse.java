package org.dromara.carbon.vendor.openapi.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * License-scoped renewal order response for enterprise backend.
 */
@Data
public class CvOpenRenewalOrderResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;

    private Long customerId;

    private String licenseId;

    private String orderStatus;

    private String payChannel;

    private BigDecimal amount;

    private String message;

    private boolean reused;
}
