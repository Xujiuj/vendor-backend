package org.dromara.carbon.vendor.renewal.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor renewal order view object.
 */
@Data
@AutoMapper(target = CvRenewalOrder.class)
public class CvRenewalOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    private Long id;

    /**
     * Renewal order number.
     */
    private String orderNo;

    /**
     * Customer identifier.
     */
    private Long customerId;

    /**
     * Original license identifier.
     */
    private String licenseId;

    /**
     * Installation identifier from the original license.
     */
    private String installId;

    /**
     * Requested renewal package identifier.
     */
    private Long requestedPackageId;

    /**
     * Requested renewal package name.
     */
    private String requestedPackageName;

    /**
     * Requested renewal edition.
     */
    private String requestedEdition;

    /**
     * Requested renewal period.
     */
    private String renewalPeriod;

    /**
     * Request contact name.
     */
    private String contactName;

    /**
     * Request contact email.
     */
    private String contactEmail;

    /**
     * Request contact phone.
     */
    private String contactPhone;

    /**
     * External idempotency key for open API retries.
     */
    private String idempotencyKey;

    /**
     * Request source.
     */
    private String requestSource;

    /**
     * Order lifecycle status.
     */
    private String orderStatus;

    /**
     * Renewal license issue status.
     */
    private String issueStatus;

    /**
     * Payment channel.
     */
    private String payChannel;

    /**
     * Order amount.
     */
    private BigDecimal amount;

    /**
     * Payment currency.
     */
    private String currency;

    /**
     * Third-party payment trade number.
     */
    private String payTradeNo;

    /**
     * Payment form or QR payload.
     */
    private String payForm;

    /**
     * Vendor cashier URL.
     */
    private String payUrl;

    /**
     * Payment timestamp.
     */
    private Date paidTime;

    /**
     * Issued renewal license identifier.
     */
    private String issuedLicenseId;

    /**
     * Creation time.
     */
    private Date createTime;

    /**
     * Last update time.
     */
    private Date updateTime;
}
