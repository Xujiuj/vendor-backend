package org.dromara.carbon.vendor.domain.open;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * License-scoped open report template list response.
 */
@Data
public class CvOpenReportTemplateListResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private List<CvOpenReportTemplateVo> templates = new ArrayList<>();
}
