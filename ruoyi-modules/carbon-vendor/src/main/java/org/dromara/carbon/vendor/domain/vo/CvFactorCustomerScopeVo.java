package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvFactorCustomerScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor factor customer scope view object.
 */
@Data
@AutoMapper(target = CvFactorCustomerScope.class)
public class CvFactorCustomerScopeVo implements Serializable {

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
     * Customer identifier.
     */
    private Long customerId;

    /**
     * Edition code.
     */
    private String edition;

    /**
     * License identifier.
     */
    private String licenseId;

    /**
     * Scope lifecycle status.
     */
    private String scopeStatus;

    /**
     * Creation time.
     */
    private Date createTime;
}
