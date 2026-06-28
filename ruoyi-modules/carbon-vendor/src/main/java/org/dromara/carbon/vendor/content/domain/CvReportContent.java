package org.dromara.carbon.vendor.content.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report content catalog configuration.
 */
@Data
@TableName("cv_report_content")
public class CvReportContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Integer directoryNo;

    private String directoryName;

    private Integer subdirectoryNo;

    private String subdirectoryName;

    private String chartNames;

    private Integer displayOrder;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String remark;
}
