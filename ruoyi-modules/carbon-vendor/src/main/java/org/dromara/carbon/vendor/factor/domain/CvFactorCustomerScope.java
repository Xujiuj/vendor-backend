package org.dromara.carbon.vendor.factor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("cv_factor_customer_scope")
public class CvFactorCustomerScope implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Long versionId;
    private Long packageId;
    private String packageName;
    private String scopeStatus;
    private Date createTime;
}
