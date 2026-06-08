package org.dromara.carbon.vendor.domain.renewal;

import lombok.Data;

import java.util.Date;

/**
 * Dedicated renewal payment callback payload.
 */
@Data
public class CvRenewalCallbackRequest {

    /**
     * Renewal order primary key.
     */
    private Long id;

    /**
     * Renewal order number.
     */
    private String orderNo;

    /**
     * Optional customer consistency check.
     */
    private Long customerId;

    /**
     * Explicit callback target status.
     */
    private String orderStatus;

    /**
     * Payment channel.
     */
    private String payChannel;

    /**
     * Payment timestamp.
     */
    private Date paidTime;

    /**
     * Issued renewal license identifier.
     */
    private String issuedLicenseId;
}
