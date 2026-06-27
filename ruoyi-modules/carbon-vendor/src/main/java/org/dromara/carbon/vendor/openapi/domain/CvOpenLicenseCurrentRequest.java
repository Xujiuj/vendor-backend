package org.dromara.carbon.vendor.openapi.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * License-scoped current license request from enterprise backend.
 */
@Data
public class CvOpenLicenseCurrentRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "licenseId cannot be blank")
    private String licenseId;

    @NotBlank(message = "installId cannot be blank")
    private String installId;

    private String keyId;

    private String currentSummary;
}
