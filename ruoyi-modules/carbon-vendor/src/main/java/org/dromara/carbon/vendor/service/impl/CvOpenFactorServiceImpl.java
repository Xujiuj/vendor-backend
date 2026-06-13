package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorRecordVo;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.service.ICvFactorCustomerScopeService;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.ICvOpenFactorService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Vendor open factor sync service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenFactorServiceImpl implements ICvOpenFactorService {

    private static final String API_PATH = "/open/factors";
    private static final String HTTP_METHOD = "GET";
    private static final String FEATURE_FACTOR_SYNC = "factor-sync";
    private static final String ISSUE_STATUS_REVOKED = "revoked";

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvFactorVersionMapper factorVersionMapper;
    private final CvFactorRecordMapper factorRecordMapper;
    private final ICvFactorCustomerScopeService factorCustomerScopeService;
    private final ICvOpenApiAuditService openApiAuditService;

    @Override
    public CvOpenFactorSyncResponse syncFactors(CvOpenFactorSyncRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();
            CvOpenLicenseFeatureSupport.requireFeature(entitlement, FEATURE_FACTOR_SYNC);
            CvFactorVersion version = findLatestAuthorizedVersion(entitlement);
            List<CvOpenFactorRecordVo> records = factorRecordMapper.selectList(Wrappers.<CvFactorRecord>lambdaQuery()
                    .eq(CvFactorRecord::getVersionId, version.getId())
                    .eq(CvFactorRecord::getEnabledFlag, Boolean.TRUE)
                    .orderByAsc(CvFactorRecord::getFactorCode)
                    .orderByAsc(CvFactorRecord::getId))
                .stream()
                .map(this::toRecordVo)
                .toList();

            CvOpenFactorSyncResponse response = new CvOpenFactorSyncResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setVendorVersionId(String.valueOf(version.getId()));
            response.setVersionCode(version.getVersionCode());
            response.setVersionName(version.getVersionName());
            response.setPublishStatus(version.getPublishStatus());
            response.setFrozenFlag(version.getFrozenFlag());
            response.setPublishedTime(version.getPublishedTime());
            response.setChanged(!version.getVersionCode().equals(request.getCurrentVersionCode()));
            response.setRecords(records);
            openApiAuditService.recordSuccess(API_PATH, HTTP_METHOD, request.getLicenseId(), request.getInstallId(),
                customerId, requestSummary(request));
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_PATH, HTTP_METHOD, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, requestSummary(request), ex.getMessage());
            throw ex;
        }
    }

    private CvLicenseIssue requireActiveLicense(CvOpenFactorSyncRequest request) {
        String licenseId = normalizeRequired(request.getLicenseId(), "licenseId cannot be blank");
        String installId = normalizeRequired(request.getInstallId(), "installId cannot be blank");
        CvLicenseIssue entitlement = licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false);
        if (entitlement == null) {
            throw new ServiceException("license entitlement does not exist");
        }
        if (!installId.equals(entitlement.getInstallId())) {
            throw new ServiceException("license installId does not match");
        }
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

    private CvFactorVersion findLatestAuthorizedVersion(CvLicenseIssue entitlement) {
        List<CvFactorVersion> versions = factorVersionMapper.selectList(Wrappers.<CvFactorVersion>lambdaQuery()
            .in(CvFactorVersion::getPublishStatus,
                CvFactorVersionLifecycleState.PUBLISHED.getStatus(),
                CvFactorVersionLifecycleState.FROZEN.getStatus())
            .orderByDesc(CvFactorVersion::getPublishedTime)
            .orderByDesc(CvFactorVersion::getId));
        return versions.stream()
            .filter(version -> isPublishedOrFrozen(version)
                && factorCustomerScopeService.isFactorVersionAuthorized(
                    version.getId(),
                    entitlement.getCustomerId(),
                    entitlement.getEdition(),
                    entitlement.getLicenseId()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("no authorized factor version for license entitlement"));
    }

    private boolean isPublishedOrFrozen(CvFactorVersion version) {
        CvFactorVersionLifecycleState state = CvFactorVersionLifecycleState.fromVersion(version);
        return state == CvFactorVersionLifecycleState.PUBLISHED || state == CvFactorVersionLifecycleState.FROZEN;
    }

    private CvOpenFactorRecordVo toRecordVo(CvFactorRecord record) {
        CvOpenFactorRecordVo vo = new CvOpenFactorRecordVo();
        vo.setFactorCode(record.getFactorCode());
        vo.setFactorName(record.getFactorName());
        vo.setFactorCategory(record.getFactorCategory());
        vo.setFactorValue(record.getFactorValue());
        vo.setFactorUnit(record.getFactorUnit());
        vo.setSourceRef(record.getSourceRef());
        return vo;
    }

    private String normalizeRequired(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    private String requestSummary(CvOpenFactorSyncRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "currentVersionCode=" + (StringUtils.isBlank(request.getCurrentVersionCode())
            ? ""
            : request.getCurrentVersionCode().trim());
    }
}
