package org.dromara.carbon.vendor.openapi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.content.domain.CvReportContent;
import org.dromara.carbon.vendor.content.mapper.CvReportContentMapper;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.license.service.impl.CvLicenseInstallBindingSupport;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentVo;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenReportContentService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Vendor open report content catalog service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenReportContentServiceImpl implements ICvOpenReportContentService {

    private static final String API_PATH = "/open/report-contents";
    private static final String HTTP_METHOD = "GET";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final String STATUS_ENABLED = "0";

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvReportContentMapper reportContentMapper;
    private final ICvOpenApiAuditService openApiAuditService;

    @Override
    public CvOpenReportContentListResponse listContents(CvOpenReportContentRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();
            List<CvOpenReportContentVo> contents = reportContentMapper.selectList(Wrappers.<CvReportContent>lambdaQuery()
                    .eq(CvReportContent::getStatus, STATUS_ENABLED)
                    .orderByAsc(CvReportContent::getDisplayOrder)
                    .orderByAsc(CvReportContent::getDirectoryNo)
                    .orderByAsc(CvReportContent::getSubdirectoryNo)
                    .orderByAsc(CvReportContent::getId))
                .stream()
                .map(this::toContentVo)
                .toList();

            CvOpenReportContentListResponse response = new CvOpenReportContentListResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setContents(contents);
            openApiAuditService.recordSuccess(API_PATH, HTTP_METHOD, request.getLicenseId(), request.getInstallId(),
                customerId, "contentCount=" + contents.size());
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_PATH, HTTP_METHOD, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, "report-content-list", ex.getMessage());
            throw ex;
        }
    }

    private CvLicenseIssue requireActiveLicense(CvOpenReportContentRequest request) {
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

    private CvOpenReportContentVo toContentVo(CvReportContent content) {
        CvOpenReportContentVo vo = new CvOpenReportContentVo();
        vo.setContentId(content.getId());
        vo.setDirectoryNo(content.getDirectoryNo());
        vo.setDirectoryName(content.getDirectoryName());
        vo.setSubdirectoryNo(content.getSubdirectoryNo());
        vo.setSubdirectoryName(content.getSubdirectoryName());
        vo.setChartNames(content.getChartNames());
        vo.setDisplayOrder(content.getDisplayOrder());
        vo.setRemark(content.getRemark());
        return vo;
    }

    private String normalizeRequired(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }
}
