package org.dromara.carbon.vendor.content.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.content.domain.CvReportContent;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor report content catalog view object.
 */
@Data
@AutoMapper(target = CvReportContent.class)
public class CvReportContentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
