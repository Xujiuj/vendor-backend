package org.dromara.carbon.vendor.service.impl;

import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.common.core.exception.ServiceException;

/**
 * Binds a manually issued license to the first enterprise install that activates it.
 */
final class CvLicenseInstallBindingSupport {

    static final String PENDING_INSTALL_ID = "__PENDING_ENTERPRISE_ACTIVATION__";

    private CvLicenseInstallBindingSupport() {
    }

    static void bindOrReject(CvLicenseIssueMapper mapper, CvLicenseIssue issue, String installId) {
        if (PENDING_INSTALL_ID.equals(issue.getInstallId())) {
            CvLicenseIssue update = new CvLicenseIssue();
            update.setId(issue.getId());
            update.setInstallId(installId);
            mapper.updateById(update);
            issue.setInstallId(installId);
            return;
        }
        if (!installId.equals(issue.getInstallId())) {
            throw new ServiceException("license installId does not match");
        }
    }
}
