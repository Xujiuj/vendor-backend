package org.dromara.carbon.vendor.customer.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor customer archive business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvCustomer.class, reverseConvertGenerate = false)
public class CvCustomerBo extends BaseEntity {

    @NotNull(message = "客户主键不能为空", groups = { EditGroup.class })
    private Long id;

    @NotBlank(message = "客户编码不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(max = 64, message = "客户编码不能超过64个字符", groups = { AddGroup.class, EditGroup.class })
    private String customerCode;

    @NotBlank(message = "客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(max = 255, message = "客户名称不能超过255个字符", groups = { AddGroup.class, EditGroup.class })
    private String customerName;

    @Size(max = 128, message = "联系人不能超过128个字符", groups = { AddGroup.class, EditGroup.class })
    private String contactName;

    @Email(message = "联系邮箱格式不正确", groups = { AddGroup.class, EditGroup.class })
    @Size(max = 255, message = "联系邮箱不能超过255个字符", groups = { AddGroup.class, EditGroup.class })
    private String contactEmail;

    @Size(max = 64, message = "联系电话不能超过64个字符", groups = { AddGroup.class, EditGroup.class })
    private String contactPhone;

    @NotBlank(message = "客户状态不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(max = 32, message = "客户状态不能超过32个字符", groups = { AddGroup.class, EditGroup.class })
    private String customerStatus;

    @Size(max = 500, message = "备注不能超过500个字符", groups = { AddGroup.class, EditGroup.class })
    private String remark;
}
