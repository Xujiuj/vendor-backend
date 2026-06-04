package org.dromara.carbon.vendor.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor customer query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CvCustomerBo extends BaseEntity {

    /**
     * Customer primary key.
     */
    private Long id;

    /**
     * Stable customer code.
     */
    private String customerCode;

    /**
     * Customer display name.
     */
    private String customerName;

    /**
     * Primary contact name.
     */
    private String contactName;

    /**
     * Customer lifecycle status.
     */
    private String customerStatus;
}
