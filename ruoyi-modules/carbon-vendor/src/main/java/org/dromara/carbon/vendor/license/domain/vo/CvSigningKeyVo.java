package org.dromara.carbon.vendor.license.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor signing key view object.
 */
@Data
@AutoMapper(target = CvSigningKey.class)
public class CvSigningKeyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    private Long id;

    /**
     * Key identifier.
     */
    private String keyId;

    /**
     * Signature algorithm.
     */
    private String algorithm;

    /**
     * Public key PEM text.
     */
    private String publicKeyPem;

    /**
     * Key lifecycle status.
     */
    private String keyStatus;

    /**
     * Key validity start time.
     */
    private Date validFrom;

    /**
     * Key validity end time.
     */
    private Date validTo;

    /**
     * Creation time.
     */
    private Date createTime;
}
