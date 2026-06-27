package org.dromara.carbon.vendor.customer.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor customer archive view object.
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CvCustomer.class)
public class CvCustomerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "客户主键")
    private Long id;

    @ExcelProperty(value = "客户编码")
    private String customerCode;

    @ExcelProperty(value = "客户名称")
    private String customerName;

    @ExcelProperty(value = "联系人")
    private String contactName;

    @ExcelProperty(value = "联系邮箱")
    private String contactEmail;

    @ExcelProperty(value = "联系电话")
    private String contactPhone;

    @ExcelProperty(value = "客户状态")
    private String customerStatus;

    @ExcelProperty(value = "创建时间")
    private Date createTime;

    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    @ExcelProperty(value = "备注")
    private String remark;
}
