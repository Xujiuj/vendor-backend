package org.dromara.carbon.vendor.openapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.openapi.domain.CvOpenApiAudit;
import org.dromara.carbon.vendor.openapi.mapper.CvOpenApiAuditMapper;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Vendor open API audit service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenApiAuditServiceImpl implements ICvOpenApiAuditService {

    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAILED = "failed";
    private static final int SUMMARY_LIMIT = 1000;
    private static final int ERROR_LIMIT = 1000;

    private final CvOpenApiAuditMapper baseMapper;

    @Override
    public void recordSuccess(String apiPath, String httpMethod, String licenseId, String installId,
                              Long customerId, String requestSummary) {
        insertAudit(apiPath, httpMethod, licenseId, installId, customerId, requestSummary, STATUS_SUCCESS, null);
    }

    @Override
    public void recordFailure(String apiPath, String httpMethod, String licenseId, String installId,
                              Long customerId, String requestSummary, String errorMessage) {
        insertAudit(apiPath, httpMethod, licenseId, installId, customerId, requestSummary, STATUS_FAILED, errorMessage);
    }

    private void insertAudit(String apiPath, String httpMethod, String licenseId, String installId,
                             Long customerId, String requestSummary, String responseStatus, String errorMessage) {
        CvOpenApiAudit audit = new CvOpenApiAudit();
        audit.setApiPath(apiPath);
        audit.setHttpMethod(httpMethod);
        audit.setLicenseId(trimToNull(licenseId));
        audit.setInstallId(trimToNull(installId));
        audit.setCustomerId(customerId);
        audit.setRequestSummary(limit(requestSummary, SUMMARY_LIMIT));
        audit.setResponseStatus(responseStatus);
        audit.setErrorMessage(limit(errorMessage, ERROR_LIMIT));
        audit.setCreateTime(new Date());
        baseMapper.insert(audit);
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String limit(String value, int maxLength) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
