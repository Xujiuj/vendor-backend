package org.dromara.carbon.vendor.template.domain.enums;

import org.dromara.carbon.vendor.template.domain.CvReportTemplate;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.util.Locale;

/**
 * Explicit report template lifecycle states derived from vendor-owned metadata.
 */
public enum CvReportTemplateLifecycleState {

    DRAFT("draft"),
    PUBLISHED("published"),
    DISABLED("disabled"),
    DELETED("deleted");

    private final String status;

    CvReportTemplateLifecycleState(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static CvReportTemplateLifecycleState fromTemplate(CvReportTemplate template) {
        String normalizedStatus = normalizeStatus(template.getPublishStatus());
        if (PUBLISHED.status.equals(normalizedStatus)) {
            return PUBLISHED;
        }
        if (DISABLED.status.equals(normalizedStatus)) {
            return DISABLED;
        }
        if (DRAFT.status.equals(normalizedStatus)) {
            return DRAFT;
        }
        if (DELETED.status.equals(normalizedStatus)) {
            return DELETED;
        }
        throw new ServiceException("Unsupported report template lifecycle status: " + template.getPublishStatus());
    }

    public static String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? DRAFT.status : status.trim().toLowerCase(Locale.ROOT);
    }
}
