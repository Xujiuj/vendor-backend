package org.dromara.carbon.vendor.openapi.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * License-scoped open report content catalog response.
 */
@Data
public class CvOpenReportContentListResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private List<CvOpenReportContentVo> contents = new ArrayList<>();
}
