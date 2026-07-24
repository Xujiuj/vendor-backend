package org.dromara.carbon.vendor.dimension.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Effective publication policy exposed to the vendor UI and open API.
 */
@Data
public class CvEmissionSourcePublicationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String publicationId;
    private String publishMode;
    private String versionNo;
    private List<String> publishedVersions = new ArrayList<>();
    private Integer recordCount;
    private String publishedBy;
    private Date publishedTime;
}
