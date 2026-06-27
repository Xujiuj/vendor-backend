package org.dromara.carbon.vendor.openapi.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * License-scoped renewal order request from enterprise backend.
 */
@Data
public class CvOpenRenewalOrderRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "licenseId cannot be blank")
    private String licenseId;

    @NotBlank(message = "installId cannot be blank")
    private String installId;

    private Long packageId;

    private String edition;

    private String renewalPeriod;

    private String contactName;

    private String contactEmail;

    private String contactPhone;

    private String idempotencyKey;
}
