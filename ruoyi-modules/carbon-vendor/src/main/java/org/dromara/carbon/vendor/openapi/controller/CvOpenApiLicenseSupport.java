package org.dromara.carbon.vendor.openapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Open API helpers shared by license-scoped controllers.
 */
final class CvOpenApiLicenseSupport {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private CvOpenApiLicenseSupport() {
    }

    static String resolveLicenseId(String requestLicenseId, HttpServletRequest request) {
        if (StringUtils.isNotBlank(requestLicenseId)) {
            return requestLicenseId;
        }
        String authorization = request == null ? null : request.getHeader(AUTHORIZATION);
        if (StringUtils.isBlank(authorization) || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return requestLicenseId;
        }
        String bearer = authorization.substring(BEARER_PREFIX.length()).trim();
        return StringUtils.isBlank(bearer) ? requestLicenseId : bearer;
    }

    static String resolveLicenseId(String requestLicenseId) {
        return resolveLicenseId(requestLicenseId, currentRequest());
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
