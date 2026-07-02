package org.dromara.carbon.vendor.license.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
            update.setInstallId(installId);
            int updated = mapper.update(update, Wrappers.<CvLicenseIssue>lambdaUpdate()
                .eq(CvLicenseIssue::getId, issue.getId())
                .eq(CvLicenseIssue::getInstallId, PENDING_INSTALL_ID));
            if (updated == 1) {
                issue.setInstallId(installId);
                return;
            }
            CvLicenseIssue current = mapper.selectById(issue.getId());
            if (current != null && installId.equals(current.getInstallId())) {
                issue.setInstallId(installId);
                return;
            }
            throw new ServiceException("授权文件的部署指纹与本机不匹配");
        }
        if (!installId.equals(issue.getInstallId())) {
            throw new ServiceException("授权文件的部署指纹与本机不匹配");
        }
    }
}
