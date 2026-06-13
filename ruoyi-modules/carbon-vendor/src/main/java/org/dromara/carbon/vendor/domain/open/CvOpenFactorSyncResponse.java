package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * License-scoped open factor sync response.
 */
@Data
public class CvOpenFactorSyncResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private String vendorVersionId;

    private String versionCode;

    private String versionName;

    private String publishStatus;

    private Boolean frozenFlag;

    private Date publishedTime;

    private boolean changed;

    private List<CvOpenFactorRecordVo> records = new ArrayList<>();
}
