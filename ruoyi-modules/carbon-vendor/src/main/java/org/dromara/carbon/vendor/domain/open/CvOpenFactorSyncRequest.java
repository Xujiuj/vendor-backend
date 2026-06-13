package org.dromara.carbon.vendor.domain.open;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * License-scoped open factor sync request from enterprise backend.
 */
@Data
public class CvOpenFactorSyncRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "licenseId cannot be blank")
    private String licenseId;

    @NotBlank(message = "installId cannot be blank")
    private String installId;

    private String currentVersionCode;
}
