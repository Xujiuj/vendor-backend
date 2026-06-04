package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor signing key metadata cv_signing_key.
 */
@Data
@TableName("cv_signing_key")
public class CvSigningKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
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

    /**
     * Creation time.
     */
    private Date createTime;
}
