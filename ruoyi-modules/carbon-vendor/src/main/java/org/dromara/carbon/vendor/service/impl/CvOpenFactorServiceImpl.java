package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvVendorTableField;
import org.dromara.carbon.vendor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorRecordVo;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenTableFieldDefinitionVo;
import org.dromara.carbon.vendor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.service.ICvFactorCustomerScopeService;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.ICvOpenFactorService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final Set<String> ALLOWED_FACTOR_TABLE_CODES = Set.of(
        "202ef",
        "203ef",
        "205ef",
        "206"
    );

    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvFactorVersionMapper factorVersionMapper;
    private final CvFactorRecordMapper factorRecordMapper;
    private final CvVendorTableFieldMapper tableFieldMapper;
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
            List<CvVendorTableField> fieldDefinitions = tableFieldMapper.selectList(Wrappers.<CvVendorTableField>lambdaQuery()
                .eq(CvVendorTableField::getTableGroup, "factor")
                .in(CvVendorTableField::getTableCode, ALLOWED_FACTOR_TABLE_CODES)
                .eq(CvVendorTableField::getStatus, "0")
                .orderByAsc(CvVendorTableField::getTableCode)
                .orderByAsc(CvVendorTableField::getSortOrder)
                .orderByAsc(CvVendorTableField::getId));
            Map<String, Set<String>> allowedCustomFieldKeys = fieldDefinitions.stream()
                .filter(field -> !builtInFactorFields().contains(field.getFieldKey()))
                .collect(Collectors.groupingBy(
                    CvVendorTableField::getTableCode,
                    Collectors.mapping(CvVendorTableField::getFieldKey, Collectors.toCollection(java.util.LinkedHashSet::new))));
            List<CvOpenFactorRecordVo> records = factorRecordMapper.selectList(Wrappers.<CvFactorRecord>lambdaQuery()
                    .eq(CvFactorRecord::getVersionId, version.getId())
                    .in(CvFactorRecord::getFactorTableCode, ALLOWED_FACTOR_TABLE_CODES)
                    .eq(CvFactorRecord::getEnabledFlag, Boolean.TRUE)
                    .orderByAsc(CvFactorRecord::getFactorTableCode)
                    .orderByAsc(CvFactorRecord::getFactorCode)
                    .orderByAsc(CvFactorRecord::getId))
                .stream()
                .map(record -> toRecordVo(record, allowedCustomFieldKeys.getOrDefault(record.getFactorTableCode(), Set.of())))
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
            response.setFieldDefinitions(fieldDefinitions.stream().map(this::toFieldDefinitionVo).toList());
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

    private CvOpenFactorRecordVo toRecordVo(CvFactorRecord record, Set<String> allowedCustomFieldKeys) {
        CvOpenFactorRecordVo vo = new CvOpenFactorRecordVo();
        vo.setFactorCode(record.getFactorCode());
        vo.setFactorTableCode(record.getFactorTableCode());
        vo.setFactorName(record.getFactorName());
        vo.setFactorCategory(record.getFactorCategory());
        vo.setFactorValue(record.getFactorValue());
        vo.setFactorUnit(record.getFactorUnit());
        vo.setFactorKey(record.getFactorKey());
        vo.setEmissionSourceName(record.getEmissionSourceName());
        vo.setEmissionSourceNameEn(record.getEmissionSourceNameEn());
        vo.setFuelMaterialCategory(record.getFuelMaterialCategory());
        vo.setSourceUnit(record.getSourceUnit());
        vo.setCo2(record.getCo2());
        vo.setCh4(record.getCh4());
        vo.setN2o(record.getN2o());
        vo.setHfcs(record.getHfcs());
        vo.setPfcs(record.getPfcs());
        vo.setSf6(record.getSf6());
        vo.setNf3(record.getNf3());
        vo.setApplicableScope(record.getApplicableScope());
        vo.setFactorSource(record.getFactorSource());
        vo.setGwpCh4(record.getGwpCh4());
        vo.setGwpN2o(record.getGwpN2o());
        vo.setGwpHfcs(record.getGwpHfcs());
        vo.setGwpPfcs(record.getGwpPfcs());
        vo.setGwpSf6(record.getGwpSf6());
        vo.setGwpNf3(record.getGwpNf3());
        vo.setFactorGwp(record.getFactorGwp());
        vo.setVersionProvinceCode(record.getVersionProvinceCode());
        vo.setFactorVersion(record.getFactorVersion());
        vo.setDivisionCode(record.getDivisionCode());
        vo.setDivisionName(record.getDivisionName());
        vo.setRegionName(record.getRegionName());
        vo.setProvinceFactor(record.getProvinceFactor());
        vo.setRegionFactor(record.getRegionFactor());
        vo.setNationalFactor(record.getNationalFactor());
        vo.setNonFossilExcludedFactor(record.getNonFossilExcludedFactor());
        vo.setNationalFossilPowerFactor(record.getNationalFossilPowerFactor());
        vo.setRowNo(record.getRowNo());
        vo.setFuelLevel1(record.getFuelLevel1());
        vo.setFuelLevel2(record.getFuelLevel2());
        vo.setFuelLevel3(record.getFuelLevel3());
        vo.setFuelLevel4(record.getFuelLevel4());
        vo.setLowerHeatValue(record.getLowerHeatValue());
        vo.setLowerHeatValueCv(record.getLowerHeatValueCv());
        vo.setCo2Factor(record.getCo2Factor());
        vo.setCo2FactorCv(record.getCo2FactorCv());
        vo.setGwpValue(record.getGwpValue());
        vo.setConvertedFactor(record.getConvertedFactor());
        vo.setSourceRef(record.getSourceRef());
        vo.setCustomFields(filterCustomFields(record.getCustomFields(), allowedCustomFieldKeys));
        vo.setRemark(record.getRemark());
        return vo;
    }

    private CvOpenTableFieldDefinitionVo toFieldDefinitionVo(CvVendorTableField field) {
        CvOpenTableFieldDefinitionVo vo = new CvOpenTableFieldDefinitionVo();
        vo.setTableGroup(field.getTableGroup());
        vo.setTableCode(field.getTableCode());
        vo.setFieldKey(field.getFieldKey());
        vo.setFieldLabel(field.getFieldLabel());
        vo.setFieldType(field.getFieldType());
        vo.setFieldPrecision(field.getFieldPrecision());
        vo.setFieldWidth(field.getFieldWidth());
        vo.setRequiredFlag(field.getRequiredFlag());
        vo.setSortOrder(field.getSortOrder());
        return vo;
    }

    private Map<String, Object> filterCustomFields(String customFields, Set<String> allowedCustomFieldKeys) {
        if (StringUtils.isBlank(customFields) || allowedCustomFieldKeys.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> values = JsonUtils.parseObject(customFields, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> filtered = new LinkedHashMap<>();
        for (String fieldKey : allowedCustomFieldKeys) {
            if (values.containsKey(fieldKey)) {
                filtered.put(fieldKey, values.get(fieldKey));
            }
        }
        return filtered;
    }

    private Set<String> builtInFactorFields() {
        return Set.of(
            "id", "versionId", "factorTableCode", "factorCode", "factorName", "factorCategory", "factorValue",
            "factorUnit", "factorKey", "emissionSourceName", "emissionSourceNameEn", "fuelMaterialCategory",
            "sourceUnit", "co2", "ch4", "n2o", "hfcs", "pfcs", "sf6", "nf3", "applicableScope", "factorSource",
            "gwpCh4", "gwpN2o", "gwpHfcs", "gwpPfcs", "gwpSf6", "gwpNf3", "factorGwp", "versionProvinceCode",
            "factorVersion", "divisionCode", "divisionName", "regionName", "provinceFactor", "regionFactor",
            "nationalFactor", "nonFossilExcludedFactor", "nationalFossilPowerFactor", "rowNo", "fuelLevel1",
            "fuelLevel2", "fuelLevel3", "fuelLevel4", "lowerHeatValue", "lowerHeatValueCv", "co2Factor",
            "co2FactorCv", "gwpValue", "convertedFactor", "sourceRef", "enabledFlag", "createTime",
            "updateTime", "remark", "customFields"
        );
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
