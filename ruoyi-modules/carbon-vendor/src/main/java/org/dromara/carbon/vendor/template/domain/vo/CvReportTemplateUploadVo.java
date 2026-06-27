package org.dromara.carbon.vendor.template.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Uploaded vendor report template file metadata.
 */
@Data
public class CvReportTemplateUploadVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Original file name after safety normalization.
     */
    private String fileName;

    /**
     * Vendor-local readable file URI.
     */
    private String fileUri;

    /**
     * Uploaded file size in bytes.
     */
    private Long size;
}
