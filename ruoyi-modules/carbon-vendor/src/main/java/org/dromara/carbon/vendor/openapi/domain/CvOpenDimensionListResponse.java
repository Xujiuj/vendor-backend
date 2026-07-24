package org.dromara.carbon.vendor.openapi.domain;

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

    /** Vendor-controlled publication boundary for 103 version synchronization. */
    private String publicationId;

    private String publishMode;

    private List<String> publishedVersions = new ArrayList<>();

    private List<CvOpenDimensionRecordVo> records = new ArrayList<>();
}
