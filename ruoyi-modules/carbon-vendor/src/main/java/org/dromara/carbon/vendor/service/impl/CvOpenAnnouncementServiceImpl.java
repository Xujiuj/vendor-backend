package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementVo;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.service.ICvOpenAnnouncementService;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysNotice;
import org.dromara.system.mapper.SysNoticeMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Vendor open announcement service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenAnnouncementServiceImpl implements ICvOpenAnnouncementService {

    private static final String API_PATH = "/open/announcements";
    private static final String HTTP_METHOD = "GET";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final SysNoticeMapper noticeMapper;
    private final ICvOpenApiAuditService openApiAuditService;

    @Override
    public CvOpenAnnouncementListResponse listAnnouncements(CvOpenAnnouncementRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();
            int limit = normalizeLimit(request.getLimit());
            List<CvOpenAnnouncementVo> announcements = TenantHelper.ignore(() -> noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
                    .eq(SysNotice::getStatus, "0")
                    .orderByDesc(SysNotice::getCreateTime)
                    .orderByDesc(SysNotice::getNoticeId))
                .stream()
                .limit(limit)
                .map(this::toAnnouncementVo)
                .toList());

            CvOpenAnnouncementListResponse response = new CvOpenAnnouncementListResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setAnnouncements(announcements);
            openApiAuditService.recordSuccess(API_PATH, HTTP_METHOD, request.getLicenseId(), request.getInstallId(),
                customerId, requestSummary(request));
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_PATH, HTTP_METHOD, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, requestSummary(request), ex.getMessage());
            throw ex;
        }
    }

    private CvLicenseIssue requireActiveLicense(CvOpenAnnouncementRequest request) {
        String licenseId = normalizeRequired(request.getLicenseId(), "licenseId cannot be blank");
        String installId = normalizeRequired(request.getInstallId(), "installId cannot be blank");
        CvLicenseIssue entitlement = licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false);
        if (entitlement == null) {
            throw new ServiceException("license entitlement does not exist");
        }
        CvLicenseInstallBindingSupport.bindOrReject(licenseIssueMapper, entitlement, installId);
        if (entitlement.getRevokedTime() != null || ISSUE_STATUS_REVOKED.equalsIgnoreCase(entitlement.getIssueStatus())) {
            throw new ServiceException("license entitlement is revoked");
        }
        Date now = new Date();
        if ((entitlement.getValidFrom() != null && entitlement.getValidFrom().after(now))
            || (entitlement.getValidTo() != null && entitlement.getValidTo().before(now))) {
            throw new ServiceException("license entitlement is not currently valid");
        }
        return entitlement;
    }

    private CvOpenAnnouncementVo toAnnouncementVo(SysNotice notice) {
        CvOpenAnnouncementVo vo = new CvOpenAnnouncementVo();
        vo.setNoticeId(notice.getNoticeId());
        vo.setNoticeTitle(notice.getNoticeTitle());
        vo.setNoticeType(notice.getNoticeType());
        vo.setNoticeContent(notice.getNoticeContent());
        vo.setStatus(notice.getStatus());
        vo.setRemark(notice.getRemark());
        vo.setCreateTime(notice.getCreateTime());
        return vo;
    }

    private String normalizeRequired(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String requestSummary(CvOpenAnnouncementRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "limit=" + normalizeLimit(request.getLimit());
    }
}
