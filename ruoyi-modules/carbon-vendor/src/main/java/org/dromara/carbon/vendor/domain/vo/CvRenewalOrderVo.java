package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;

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
     * Order lifecycle status.
     */
    private String orderStatus;

    /**
     * Payment channel.
     */
    private String payChannel;

    /**
     * Order amount.
     */
    private BigDecimal amount;

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
