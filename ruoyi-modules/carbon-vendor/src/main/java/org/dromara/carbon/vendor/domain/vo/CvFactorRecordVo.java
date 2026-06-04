package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvFactorRecord;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor factor record view object.
 */
@Data
@AutoMapper(target = CvFactorRecord.class)
public class CvFactorRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    private Long id;

    /**
     * Factor version identifier.
     */
    private Long versionId;

    /**
     * Stable factor code.
     */
    private String factorCode;

    /**
     * Factor display name.
     */
    private String factorName;

    /**
     * Factor category.
     */
    private String factorCategory;

    /**
     * Factor value.
     */
    private BigDecimal factorValue;

    /**
     * Factor unit.
     */
    private String factorUnit;

    /**
     * Source reference.
     */
    private String sourceRef;

    /**
     * Whether this factor is enabled.
     */
    private Boolean enabledFlag;

    /**
     * Creation time.
     */
    private Date createTime;
}
