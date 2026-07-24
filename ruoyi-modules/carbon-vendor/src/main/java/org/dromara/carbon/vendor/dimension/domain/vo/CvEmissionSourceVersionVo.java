package org.dromara.carbon.vendor.dimension.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Selectable 103 version summary.
 */
@Data
public class CvEmissionSourceVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String versionNo;
    private LocalDate effectiveDate;
    private Integer recordCount;
}
