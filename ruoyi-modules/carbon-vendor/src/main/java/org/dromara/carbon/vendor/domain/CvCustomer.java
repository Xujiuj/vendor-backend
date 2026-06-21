package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Vendor managed customer account.
 */
@Data
@TableName("cv_customer")
public class CvCustomer {

    @TableId(value = "id")
    private Long id;

    private String customerCode;
    private String customerName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String customerStatus;
    private Date createTime;
    private Date updateTime;
    private String remark;

}
