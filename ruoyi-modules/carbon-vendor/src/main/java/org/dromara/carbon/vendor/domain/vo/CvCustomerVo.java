package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvCustomer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor customer view object.
 */
@Data
@AutoMapper(target = CvCustomer.class)
public class CvCustomerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * Primary contact email.
     */
    private String contactEmail;

    /**
     * Primary contact phone.
     */
    private String contactPhone;

    /**
     * Customer lifecycle status.
     */
    private String customerStatus;

    /**
     * Creation time.
     */
    private Date createTime;

    /**
     * Last update time.
     */
    private Date updateTime;

    /**
     * Remark.
     */
    private String remark;
}
