package org.dromara.carbon.vendor.license.service.impl;

import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.common.core.exception.ServiceException;

/**
 * Binds a manually issued license to the first enterprise install that activates it.
 */
public final class CvLicenseInstallBindingSupport {

    public static final String PENDING_INSTALL_ID = "__PENDING_ENTERPRISE_ACTIVATION__";

    private CvLicenseInstallBindingSupport() {
    }

    public static void bindOrReject(CvLicenseIssueMapper mapper, CvLicenseIssue issue, String installId) {
        if (PENDING_INSTALL_ID.equals(issue.getInstallId())) {
            CvLicenseIssue update = new CvLicenseIssue();
            update.setId(issue.getId());
            update.setInstallId(installId);
            mapper.updateById(update);
            issue.setInstallId(installId);
            return;
        }
        if (!installId.equals(issue.getInstallId())) {
            throw new ServiceException("授权文件的部署指纹与本机不匹配");
        }
    }
}
