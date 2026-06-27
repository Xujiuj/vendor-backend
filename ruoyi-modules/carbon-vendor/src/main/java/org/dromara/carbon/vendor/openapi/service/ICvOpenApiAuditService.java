package org.dromara.carbon.vendor.openapi.service;

/**
 * Records vendor open API calls without storing enterprise business details.
 */
public interface ICvOpenApiAuditService {

    void recordSuccess(String apiPath, String httpMethod, String licenseId, String installId,
                       Long customerId, String requestSummary);

    void recordFailure(String apiPath, String httpMethod, String licenseId, String installId,
                       Long customerId, String requestSummary, String errorMessage);
}
