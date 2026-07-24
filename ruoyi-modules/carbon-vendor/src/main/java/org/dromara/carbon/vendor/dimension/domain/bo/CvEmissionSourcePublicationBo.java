package org.dromara.carbon.vendor.dimension.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Publication scope for vendor 103 emission-source categories.
 */
@Data
public class CvEmissionSourcePublicationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "publishMode cannot be blank")
    private String publishMode;

    private String versionNo;
}
