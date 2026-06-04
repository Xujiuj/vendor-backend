package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor managed customer account cv_customer.
 */
@Data
@TableName("cv_customer")
public class CvCustomer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Customer primary key.
     */
    @TableId(value = "id")
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
