package org.dromara.carbon.vendor.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Vendor open API audit record cv_open_api_audit.
 */
@Data
@TableName("cv_open_api_audit")
public class CvOpenApiAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Primary key.
     */
    @TableId(value = "id")
    private Long id;

    /**
     * Open API path.
     */
    private String apiPath;

    /**
     * HTTP method.
     */
    private String httpMethod;

    /**
     * Request license id.
     */
    private String licenseId;

    /**
     * Request install id.
     */
    private String installId;

    /**
     * Vendor customer id resolved from license.
     */
    private Long customerId;

    /**
     * Sanitized request summary. Never store enterprise business detail here.
     */
    private String requestSummary;

    /**
     * success or failed.
     */
    private String responseStatus;

    /**
     * Failure message when responseStatus is failed.
     */
    private String errorMessage;

    /**
     * Remote address when available.
     */
    private String remoteAddr;

    /**
     * Creation time.
     */
    private Date createTime;
}
