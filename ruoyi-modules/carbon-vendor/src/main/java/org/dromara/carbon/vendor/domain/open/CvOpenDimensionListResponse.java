package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * License-scoped open dimension list response.
 */
@Data
public class CvOpenDimensionListResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private String dimensionCode;

    private long total;

    private List<CvOpenDimensionRecordVo> records = new ArrayList<>();
}
