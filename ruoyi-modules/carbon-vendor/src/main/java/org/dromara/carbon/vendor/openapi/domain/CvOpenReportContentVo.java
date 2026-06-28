package org.dromara.carbon.vendor.openapi.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Vendor report content catalog record exposed to enterprise backend.
 */
@Data
public class CvOpenReportContentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long contentId;

    private Integer directoryNo;

    private String directoryName;

    private Integer subdirectoryNo;

    private String subdirectoryName;

    private String chartNames;

    private Integer displayOrder;

    private String remark;
}
