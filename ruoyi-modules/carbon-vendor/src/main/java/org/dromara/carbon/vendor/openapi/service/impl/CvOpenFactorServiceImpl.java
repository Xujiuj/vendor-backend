package org.dromara.carbon.vendor.openapi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactor;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorScope;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorVersion;
import org.dromara.carbon.vendor.dimension.domain.CvGreenhouseGas;
import org.dromara.carbon.vendor.factor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorRecordVo;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.factor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.license.service.impl.CvLicenseInstallBindingSupport;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.factor.service.ICvFactorCustomerScopeService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenFactorService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final CvElectricityMapper electricityMapper;
    private final CvElectricityFactorVersionMapper electricityFactorVersionMapper;
    private final CvElectricityFactorScopeMapper electricityFactorScopeMapper;
    private final CvGreenhouseGasMapper greenhouseGasMapper;
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
            List<CvOpenFactorRecordVo> records = querySourceAFactorRecords();

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
                    entitlement.getPackageId(),
                    entitlement.getEdition(),
                    entitlement.getLicenseId()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("no authorized factor version for license entitlement"));
    }

    private boolean isPublishedOrFrozen(CvFactorVersion version) {
        CvFactorVersionLifecycleState state = CvFactorVersionLifecycleState.fromVersion(version);
        return state == CvFactorVersionLifecycleState.PUBLISHED || state == CvFactorVersionLifecycleState.FROZEN;
    }

    private List<CvOpenFactorRecordVo> querySourceAFactorRecords() {
        List<CvOpenFactorRecordVo> records = new ArrayList<>();
        electricityMapper.selectList(Wrappers.<CvElectricityFactor>lambdaQuery()
                .eq(CvElectricityFactor::getStatus, "0")
                .orderByAsc(CvElectricityFactor::getSortOrder)
                .orderByAsc(CvElectricityFactor::getId))
            .stream()
            .map(this::toElectricityFactorRecord)
            .forEach(records::add);
        electricityFactorVersionMapper.selectList(Wrappers.<CvElectricityFactorVersion>lambdaQuery()
                .eq(CvElectricityFactorVersion::getStatus, "0")
                .orderByAsc(CvElectricityFactorVersion::getSortOrder)
                .orderByAsc(CvElectricityFactorVersion::getId))
            .stream()
            .map(this::toElectricityVersionRecord)
            .forEach(records::add);
        electricityFactorScopeMapper.selectList(Wrappers.<CvElectricityFactorScope>lambdaQuery()
                .eq(CvElectricityFactorScope::getStatus, "0")
                .orderByAsc(CvElectricityFactorScope::getSortOrder)
                .orderByAsc(CvElectricityFactorScope::getId))
            .stream()
            .map(this::toElectricityScopeRecord)
            .forEach(records::add);
        greenhouseGasMapper.selectList(Wrappers.<CvGreenhouseGas>lambdaQuery()
                .eq(CvGreenhouseGas::getStatus, "0")
                .orderByAsc(CvGreenhouseGas::getSortOrder)
                .orderByAsc(CvGreenhouseGas::getId))
            .stream()
            .map(this::toGreenhouseGasRecord)
            .forEach(records::add);
        return records;
    }

    private CvOpenFactorRecordVo toElectricityFactorRecord(CvElectricityFactor record) {
        CvOpenFactorRecordVo vo = new CvOpenFactorRecordVo();
        vo.setFactorTableCode("202ef");
        vo.setFactorCode(record.getFactorVersion() + ":" + record.getDivisionCode());
        vo.setFactorName(record.getDivisionName());
        vo.setFactorCategory("ef-electricity-factor");
        vo.setFactorValue(firstNumber(record.getProvinceFactor(), record.getRegionFactor(), record.getNationalFactor()));
        vo.setFactorUnit("kgCO2e/kWh");
        vo.setVersionProvinceCode(vo.getFactorCode());
        vo.setFactorVersion(record.getFactorVersion());
        vo.setDivisionCode(record.getDivisionCode());
        vo.setDivisionName(record.getDivisionName());
        vo.setRegionName(record.getRegionName());
        vo.setProvinceFactor(record.getProvinceFactor());
        vo.setRegionFactor(record.getRegionFactor());
        vo.setNationalFactor(record.getNationalFactor());
        vo.setNonFossilExcludedFactor(record.getNonFossilExcludedFactor());
        vo.setNationalFossilPowerFactor(record.getNationalFossilPowerFactor());
        vo.setSourceRef("cv_electricity_factor");
        vo.setRemark(record.getRemark());
        return vo;
    }

    private CvOpenFactorRecordVo toElectricityVersionRecord(CvElectricityFactorVersion record) {
        CvOpenFactorRecordVo vo = new CvOpenFactorRecordVo();
        vo.setFactorTableCode("203ef");
        vo.setFactorCode(record.getFactorVersion() + ":" + record.getEffectiveYear());
        vo.setFactorName(record.getFactorVersion());
        vo.setFactorCategory("ef-electricity-version");
        vo.setFactorValue(BigDecimal.valueOf(record.getEffectiveYear()));
        vo.setFactorUnit("year");
        vo.setFactorVersion(record.getFactorVersion());
        vo.setSourceRef("cv_electricity_factor_version");
        vo.setRemark(record.getRemark());
        return vo;
    }

    private CvOpenFactorRecordVo toElectricityScopeRecord(CvElectricityFactorScope record) {
        CvOpenFactorRecordVo vo = new CvOpenFactorRecordVo();
        vo.setFactorTableCode("205ef");
        vo.setFactorCode(record.getScopeKey());
        vo.setFactorName(record.getScopeName());
        vo.setFactorCategory("ef-electricity-scope");
        vo.setFactorUnit("scope");
        vo.setFactorKey(record.getScopeKey());
        vo.setApplicableScope(record.getScopeName());
        vo.setSourceRef("cv_electricity_factor_scope");
        vo.setRemark(record.getRemark());
        return vo;
    }

    private CvOpenFactorRecordVo toGreenhouseGasRecord(CvGreenhouseGas record) {
        CvOpenFactorRecordVo vo = new CvOpenFactorRecordVo();
        vo.setFactorTableCode("206");
        vo.setFactorCode(record.getGasCode());
        vo.setFactorName(record.getGasName());
        vo.setFactorCategory("greenhouse-gas");
        vo.setFactorValue(record.getGwpValue());
        vo.setFactorUnit("GWP");
        vo.setFactorKey(record.getGasCode());
        vo.setEmissionSourceName(record.getGasName());
        vo.setEmissionSourceNameEn(record.getGasNameEn());
        vo.setFactorSource(record.getGwpVersion());
        vo.setGwpValue(record.getGwpValue());
        vo.setSourceRef("cv_greenhouse_gas");
        vo.setRemark(record.getRemark());
        return vo;
    }

    private BigDecimal firstNumber(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }
        return BigDecimal.ZERO;
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
