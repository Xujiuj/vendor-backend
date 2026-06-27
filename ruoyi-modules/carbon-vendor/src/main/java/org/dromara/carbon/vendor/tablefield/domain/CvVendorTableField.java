package org.dromara.carbon.vendor.tablefield.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("cv_vendor_table_field")
public class CvVendorTableField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String tableGroup;
    private String tableCode;
    private String fieldKey;
    private String fieldLabel;
    private String fieldType;
    private Integer fieldPrecision;
    private Integer fieldWidth;
    private String fieldOptions;
    private Boolean requiredFlag;
    private Integer sortOrder;
    private String status;
    private Date createTime;
    private Date updateTime;
    private String remark;
}
