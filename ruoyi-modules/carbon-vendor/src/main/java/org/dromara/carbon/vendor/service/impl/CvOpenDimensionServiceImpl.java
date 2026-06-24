package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvDimensionRecord;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.open.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenDimensionRecordVo;
import org.dromara.carbon.vendor.domain.open.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.mapper.CvDimensionRecordMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.ICvOpenDimensionService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Vendor open dimension service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvOpenDimensionServiceImpl implements ICvOpenDimensionService {

    private static final String API_PATH = "/open/dimensions";
    private static final String HTTP_METHOD = "GET";
    private static final String ISSUE_STATUS_REVOKED = "revoked";
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 500;
    private static final Set<String> ALLOWED_DIMENSION_CODES = Set.of(
        "admin-division",
        "emission-source-category",
        "base-year",
        "ef-electricity-factor",
        "ef-electricity-version",
        "ef-electricity-scope",
        "greenhouse-gas",
        "report-template-download"
    );

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvDimensionRecordMapper dimensionRecordMapper;
    private final ICvOpenApiAuditService openApiAuditService;

    @Override
    public CvOpenDimensionListResponse listDimensions(CvOpenDimensionRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();

            Page<CvDimensionRecord> page = dimensionRecordMapper.selectPage(
                new Page<>(normalizePageNum(request.getPageNum()), normalizePageSize(request.getPageSize())),
                buildQueryWrapper(request)
            );

            CvOpenDimensionListResponse response = new CvOpenDimensionListResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setDimensionCode(request.getDimensionCode());
            response.setTotal(page.getTotal());
            response.setRecords(toOpenRecords(page.getRecords()));
            openApiAuditService.recordSuccess(API_PATH, HTTP_METHOD, request.getLicenseId(), request.getInstallId(),
                customerId, requestSummary(request));
            return response;
        } catch (RuntimeException ex) {
            openApiAuditService.recordFailure(API_PATH, HTTP_METHOD, request == null ? null : request.getLicenseId(),
                request == null ? null : request.getInstallId(), customerId, requestSummary(request), ex.getMessage());
            throw ex;
        }
    }

    private LambdaQueryWrapper<CvDimensionRecord> buildQueryWrapper(CvOpenDimensionRequest request) {
        String dimensionCode = normalizeRequired(request.getDimensionCode(), "dimensionCode cannot be blank");
        if (!ALLOWED_DIMENSION_CODES.contains(dimensionCode)) {
            throw new ServiceException("Unsupported vendor dimension code: " + dimensionCode);
        }
        return new LambdaQueryWrapper<CvDimensionRecord>()
            .eq(CvDimensionRecord::getDimensionCode, dimensionCode)
            .like(StringUtils.isNotBlank(request.getRecordCode()), CvDimensionRecord::getRecordCode, request.getRecordCode())
            .like(StringUtils.isNotBlank(request.getRecordName()), CvDimensionRecord::getRecordName, request.getRecordName())
            .eq(StringUtils.isNotBlank(request.getParentCode()), CvDimensionRecord::getParentCode, request.getParentCode())
            .eq(CvDimensionRecord::getStatus, "0")
            .orderByAsc(CvDimensionRecord::getSortOrder)
            .orderByAsc(CvDimensionRecord::getId);
    }

    private List<CvOpenDimensionRecordVo> toOpenRecords(List<CvDimensionRecord> records) {
        return records.stream().map(this::toOpenRecord).toList();
    }

    private CvOpenDimensionRecordVo toOpenRecord(CvDimensionRecord record) {
        CvOpenDimensionRecordVo vo = new CvOpenDimensionRecordVo();
        vo.setId(record.getId());
        vo.setDimensionCode(record.getDimensionCode());
        vo.setRecordCode(record.getRecordCode());
        vo.setRecordName(record.getRecordName());
        vo.setParentCode(record.getParentCode());
        vo.setField01(record.getField01());
        vo.setField02(record.getField02());
        vo.setField03(record.getField03());
        vo.setField04(record.getField04());
        vo.setField05(record.getField05());
        vo.setField06(record.getField06());
        vo.setField07(record.getField07());
        vo.setField08(record.getField08());
        vo.setField09(record.getField09());
        vo.setField10(record.getField10());
        vo.setField11(record.getField11());
        vo.setField12(record.getField12());
        vo.setField13(record.getField13());
        vo.setField14(record.getField14());
        vo.setField15(record.getField15());
        vo.setField16(record.getField16());
        vo.setField17(record.getField17());
        vo.setField18(record.getField18());
        vo.setField19(record.getField19());
        vo.setField20(record.getField20());
        vo.setField21(record.getField21());
        vo.setField22(record.getField22());
        vo.setSortOrder(record.getSortOrder());
        vo.setStatus(record.getStatus());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());
        vo.setRemark(record.getRemark());
        return vo;
    }

    private CvLicenseIssue requireActiveLicense(CvOpenDimensionRequest request) {
        String licenseId = normalizeRequired(request.getLicenseId(), "licenseId cannot be blank");
        String installId = normalizeRequired(request.getInstallId(), "installId cannot be blank");
        CvLicenseIssue entitlement = licenseIssueMapper.selectOne(new LambdaQueryWrapper<CvLicenseIssue>()
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

    private String normalizeRequired(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    private long normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String requestSummary(CvOpenDimensionRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "dimensionCode=" + request.getDimensionCode()
            + ",recordCode=" + request.getRecordCode()
            + ",recordName=" + request.getRecordName()
            + ",parentCode=" + request.getParentCode()
            + ",pageNum=" + normalizePageNum(request.getPageNum())
            + ",pageSize=" + normalizePageSize(request.getPageSize());
    }
}
