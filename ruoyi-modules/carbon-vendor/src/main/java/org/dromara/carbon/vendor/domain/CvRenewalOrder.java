package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor renewal order placeholder cv_renewal_order.
 */
@Data
@TableName("cv_renewal_order")
public class CvRenewalOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
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
