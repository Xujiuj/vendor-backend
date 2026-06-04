package org.dromara.carbon.vendor.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor renewal order business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvRenewalOrder.class, reverseConvertGenerate = false)
public class CvRenewalOrderBo extends BaseEntity {

     /**
     * Primary key.
     */
    @NotNull(message = "id cannot be null", groups = { EditGroup.class })
    private Long id;

     /**
     * Renewal order number.
     */
    @NotBlank(message = "orderNo cannot be blank", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

     /**
     * Customer identifier.
     */
    @NotNull(message = "customerId cannot be null", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "amount cannot be null", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * Payment timestamp.
     */
    private Date paidTime;

    /**
     * Issued renewal license identifier.
     */
    private String issuedLicenseId;
}
