package org.dromara.carbon.vendor.renewal.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor online package purchase and renewal order.
 */
@Data
@TableName("cv_renewal_order")
public class CvRenewalOrder {

    @TableId(value = "id")
    private Long id;

    private String orderNo;
    private Long customerId;
    private String licenseId;
    private String installId;
    private Long requestedPackageId;
    private String requestedPackageName;
    private String requestedEdition;
    private String renewalPeriod;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String idempotencyKey;
    private String requestSource;
    private String orderStatus;
    private String issueStatus;
    private String payChannel;
    private BigDecimal amount;
    private String currency;
    private String payTradeNo;
    private String payForm;
    private String payUrl;
    private Date paidTime;
    private String issuedLicenseId;
    private Date createTime;
    private Date updateTime;

}
