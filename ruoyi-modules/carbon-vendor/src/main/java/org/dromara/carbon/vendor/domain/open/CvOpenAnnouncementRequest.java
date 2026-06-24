package org.dromara.carbon.vendor.domain.open;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * License-scoped open announcement request from enterprise backend.
 */
@Data
public class CvOpenAnnouncementRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseId;

    @NotBlank(message = "installId cannot be blank")
    private String installId;

    private Integer limit;
}
