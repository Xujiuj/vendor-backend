package org.dromara.carbon.vendor.tablefield.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.tablefield.domain.CvVendorTableField;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor table field definition view object.
 */
@Data
@AutoMapper(target = CvVendorTableField.class)
public class CvVendorTableFieldVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String tableGroup;

    private String tableCode;

    private String fieldKey;

    private String fieldLabel;

    private String fieldType;

    private Integer fieldPrecision;

    private Integer fieldWidth;

    private Boolean requiredFlag;

    private Integer sortOrder;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String remark;
}
