package org.dromara.carbon.vendor.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.domain.CvFactorVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor factor version view object.
 */
@Data
@AutoMapper(target = CvFactorVersion.class)
public class CvFactorVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    /**
     * Publish timestamp.
     */
    private Date publishedTime;

    /**
     * Creation time.
     */
    private Date createTime;

    /**
     * Remark.
     */
    private String remark;
}
