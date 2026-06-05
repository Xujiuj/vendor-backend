package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvLicenseIssueBo;
import org.dromara.carbon.vendor.domain.license.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.domain.license.CvLicenseReissueRequest;
import org.dromara.carbon.vendor.domain.license.CvLicenseIssueResult;
import org.dromara.carbon.vendor.domain.vo.CvLicenseIssueVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor license issue service.
 */
public interface ICvLicenseIssueService {

    /**
     * Query paged vendor license issue list.
     *
     * @param bo query object
     * @param pageQuery pagination query
     * @return paged license issue list
     */
    TableDataInfo<CvLicenseIssueVo> selectPageLicenseIssueList(CvLicenseIssueBo bo, PageQuery pageQuery);

    /**
     * Query license issue by id.
     *
     * @param id primary key
     * @return license issue view object
     */
    CvLicenseIssueVo selectLicenseIssueById(Long id);

    /**
     * Issue a manual license and persist the issue record.
     *
     * @param request issue request
     * @return issue result
     */
    CvLicenseIssueResult issueManualLicense(CvLicenseIssueRequest request);

    /**
     * Reissue a revoked license and persist a new append-only issue record.
     *
     * @param request reissue request
     * @return issue result
     */
    CvLicenseIssueResult reissueRevokedLicense(CvLicenseReissueRequest request);
}
