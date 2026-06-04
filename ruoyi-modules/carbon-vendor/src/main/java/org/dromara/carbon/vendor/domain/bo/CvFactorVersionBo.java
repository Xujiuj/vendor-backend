package org.dromara.carbon.vendor.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Vendor factor version query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CvFactorVersionBo extends BaseEntity {

    /**
     * Primary key.
     */
    private Long id;

    /**
     * Stable version code.
     */
    private String versionCode;

    /**
     * Version display name.
     */
    private String versionName;

    /**
     * Publish lifecycle status.
     */
    private String publishStatus;

    /**
     * Whether this version is frozen.
     */
    private Boolean frozenFlag;

    /**
     * Operator that published the version.
     */
    private String publishedBy;
}
