package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vendor factor library record cv_factor_record.
 */
@Data
@TableName("cv_factor_record")
public class CvFactorRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
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
