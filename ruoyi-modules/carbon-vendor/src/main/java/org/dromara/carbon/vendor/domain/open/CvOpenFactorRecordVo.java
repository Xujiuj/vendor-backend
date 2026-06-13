package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Open factor record returned to enterprise backend.
 */
@Data
public class CvOpenFactorRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String factorCode;

    private String factorName;

    private String factorCategory;

    private BigDecimal factorValue;

    private String factorUnit;

    private String sourceRef;
}
