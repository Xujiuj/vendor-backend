package org.dromara.carbon.vendor.license.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * Vendor signing key business object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CvSigningKey.class, reverseConvertGenerate = false)
public class CvSigningKeyBo extends BaseEntity {

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
     * Private key reference.
     */
    private String privateKeyRef;

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
}
