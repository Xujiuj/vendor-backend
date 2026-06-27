package org.dromara.carbon.vendor.openapi.service.impl;

import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.Arrays;

/**
 * Shared feature-code guard for vendor open APIs.
 */
final class CvOpenLicenseFeatureSupport {

    private CvOpenLicenseFeatureSupport() {
    }

    static void requireFeature(CvLicenseIssue issue, String featureCode) {
        if (issue == null || StringUtils.isBlank(featureCode)) {
            throw new ServiceException("license feature code is required");
        }
        String featureCodes = issue.getFeatureCodes();
        if (StringUtils.isBlank(featureCodes)) {
            throw new ServiceException("license feature is not authorized");
        }
        boolean authorized = Arrays.stream(featureCodes.split("[,;\\s]+"))
            .map(CvOpenLicenseFeatureSupport::normalizeFeatureCode)
            .filter(StringUtils::isNotBlank)
            .anyMatch(code -> featureCode.equalsIgnoreCase(code));
        if (!authorized) {
            throw new ServiceException("license feature is not authorized");
        }
    }

    private static String normalizeFeatureCode(String featureCode) {
        return featureCode == null ? "" : featureCode.trim().replace("[", "").replace("]", "").replace("\"", "");
    }
}
