package org.dromara.carbon.vendor.openapi.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * License-scoped open announcement list response.
 */
@Data
public class CvOpenAnnouncementListResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    private List<CvOpenAnnouncementVo> announcements = new ArrayList<>();
}
