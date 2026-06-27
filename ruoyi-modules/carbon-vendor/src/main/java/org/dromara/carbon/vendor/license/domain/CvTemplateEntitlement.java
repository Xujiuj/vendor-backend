package org.dromara.carbon.vendor.license.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * Report template entitlement in license.v1 payload.
 */
@Data
@JsonPropertyOrder({"templateCode", "templateVersion", "scope"})
public class CvTemplateEntitlement {

    private String templateCode;

    private String templateVersion;

    private String scope;
}
