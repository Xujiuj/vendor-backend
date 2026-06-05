package org.dromara.carbon.vendor.domain.enums;

import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.Locale;

/**
 * Explicit factor version lifecycle states derived from vendor-owned metadata.
 */
public enum CvFactorVersionLifecycleState {

    DRAFT("draft"),
    RELEASED("released"),
    FROZEN("frozen"),
    RETIRED("retired");

    private final String status;

    CvFactorVersionLifecycleState(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static CvFactorVersionLifecycleState fromVersion(CvFactorVersion version) {
        String normalizedStatus = normalizeStatus(version.getPublishStatus());
        if (RETIRED.status.equals(normalizedStatus)) {
            return RETIRED;
        }
        if (Boolean.TRUE.equals(version.getFrozenFlag())) {
            return FROZEN;
        }
        if (RELEASED.status.equals(normalizedStatus)) {
            return RELEASED;
        }
        if (DRAFT.status.equals(normalizedStatus)) {
            return DRAFT;
        }
        throw new ServiceException("Unsupported factor version lifecycle status: " + version.getPublishStatus());
    }

    public static String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? DRAFT.status : status.trim().toLowerCase(Locale.ROOT);
    }
}
