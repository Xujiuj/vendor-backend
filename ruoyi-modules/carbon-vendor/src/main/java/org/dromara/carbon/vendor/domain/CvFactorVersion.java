package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor factor library version cv_factor_version.
 */
@Data
@TableName("cv_factor_version")
public class CvFactorVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
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
