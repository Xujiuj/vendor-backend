package org.dromara.carbon.vendor.openapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.dimension.domain.CvAdminDivision;
import org.dromara.carbon.vendor.dimension.domain.CvBaseYear;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactor;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorScope;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorVersion;
import org.dromara.carbon.vendor.dimension.domain.CvEmissionSourceCategory;
import org.dromara.carbon.vendor.dimension.domain.CvGreenhouseGas;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRecordVo;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.license.service.impl.CvLicenseInstallBindingSupport;
import org.dromara.carbon.vendor.dimension.mapper.CvAdminDivisionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvBaseYearMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvEmissionSourceCategoryMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.ICvOpenDimensionService;
import org.dromara.carbon.vendor.shared.VendorManagedTableCatalog;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private final CvLicenseIssueMapper licenseIssueMapper;
    private final CvAdminDivisionMapper adminDivisionMapper;
    private final CvEmissionSourceCategoryMapper emissionSourceCategoryMapper;
    private final CvBaseYearMapper baseYearMapper;
    private final CvElectricityMapper electricityMapper;
    private final CvElectricityFactorVersionMapper electricityFactorVersionMapper;
    private final CvElectricityFactorScopeMapper electricityFactorScopeMapper;
    private final CvGreenhouseGasMapper greenhouseGasMapper;
    private final ICvOpenApiAuditService openApiAuditService;

    @Override
    public CvOpenDimensionListResponse listDimensions(CvOpenDimensionRequest request) {
        Long customerId = null;
        try {
            CvLicenseIssue entitlement = requireActiveLicense(request);
            customerId = entitlement.getCustomerId();
            String dimensionCode = normalizeRequired(request.getDimensionCode(), "dimensionCode cannot be blank");
            if (!VendorManagedTableCatalog.isOpenDimensionCode(dimensionCode)) {
                throw new ServiceException("Unsupported vendor dimension code: " + dimensionCode);
            }

            long pageNum = normalizePageNum(request.getPageNum());
            long pageSize = normalizePageSize(request.getPageSize());

            List<CvOpenDimensionRecordVo> records;
            long total;

            var result = queryStrongTyped(dimensionCode, request, pageNum, pageSize);
            records = result.records;
            total = result.total;

            CvOpenDimensionListResponse response = new CvOpenDimensionListResponse();
            response.setLicenseId(entitlement.getLicenseId());
            response.setDimensionCode(dimensionCode);
            response.setTotal(total);
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

    // ==================== Strong-typed query routing ====================

    private record QueryResult(List<CvOpenDimensionRecordVo> records, long total) {}

    private QueryResult queryStrongTyped(String dimensionCode, CvOpenDimensionRequest request, long pageNum, long pageSize) {
        return switch (dimensionCode) {
            case "admin-division" -> queryAdminDivision(request, pageNum, pageSize);
            case "emission-source-category" -> queryEmissionSourceCategory(request, pageNum, pageSize);
            case "base-year" -> queryBaseYear(request, pageNum, pageSize);
            case "ef-electricity-factor" -> queryElectricityFactor(request, pageNum, pageSize);
            case "ef-electricity-version" -> queryElectricityFactorVersion(request, pageNum, pageSize);
            case "ef-electricity-scope" -> queryElectricityFactorScope(request, pageNum, pageSize);
            case "greenhouse-gas" -> queryGreenhouseGas(request, pageNum, pageSize);
            default -> throw new ServiceException("Unsupported dimension code: " + dimensionCode);
        };
    }

    private QueryResult queryAdminDivision(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvAdminDivision> qw = new QueryWrapper<CvAdminDivision>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "division_code", req.getRecordCode())
            .like(StringUtils.isNotBlank(req.getRecordName()), "division_name", req.getRecordName())
            .eq(StringUtils.isNotBlank(req.getParentCode()), "parent_code", req.getParentCode())
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvAdminDivision> page = adminDivisionMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "admin-division", e.getDivisionCode(), e.getDivisionName(), e.getParentCode());
            vo.setLevelType(e.getLevelType());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    private QueryResult queryEmissionSourceCategory(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvEmissionSourceCategory> qw = new QueryWrapper<CvEmissionSourceCategory>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "category_code", req.getRecordCode())
            .like(StringUtils.isNotBlank(req.getRecordName()), "category_name", req.getRecordName())
            .eq(StringUtils.isNotBlank(req.getParentCode()), "parent_code", req.getParentCode())
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvEmissionSourceCategory> page = emissionSourceCategoryMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "emission-source-category", e.getCategoryCode(), e.getCategoryName(), e.getParentCode());
            vo.setCategorySk(e.getCategoryCode());
            vo.setBusinessKey(e.getBusinessKey());
            vo.setCategoryNameEn(e.getCategoryNameEn());
            vo.setGhgScope(e.getGhgScope());
            vo.setGhgScopeCategorySort(e.getSortOrder());
            vo.setGhgScopeCategory(e.getGhgScopeCategory());
            vo.setGhgScopeEn(null);
            vo.setGhgScopeCategoryEn(null);
            vo.setIsoCategory(e.getIsoCategory());
            vo.setIsoCategoryEn(e.getIsoCategoryEn());
            vo.setIsoCategoryDescription(e.getIsoCategoryDescription());
            vo.setIsoCategoryDescriptionEn(null);
            vo.setIsoCustomSubcategory(null);
            vo.setGbScopeCategory(e.getGbScopeCategory());
            vo.setGbSubcategory(e.getGbSubcategory());
            vo.setEffectiveDate(e.getEffectiveDate());
            vo.setExpireDate(e.getExpireDate());
            vo.setCurrentFlag(e.getCurrentFlag());
            vo.setVersionNo(e.getVersionNo());
            vo.setStandardCategory(e.getStandardCategory());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    private QueryResult queryBaseYear(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvBaseYear> qw = new QueryWrapper<CvBaseYear>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "base_year_key", req.getRecordCode())
            .and(StringUtils.isNotBlank(req.getRecordName()), wrapper -> wrapper
                .like("base_year", req.getRecordName())
                .or()
                .like("description", req.getRecordName()))
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvBaseYear> page = baseYearMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "base-year", e.getBaseYearKey(), String.valueOf(e.getBaseYear()), null);
            vo.setBaseYearKey(e.getBaseYearKey());
            vo.setDescription(e.getDescription());
            vo.setBaseYear(e.getBaseYear());
            vo.setIsCurrent(e.getIsCurrent());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    private QueryResult queryElectricityFactor(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvElectricityFactor> qw = new QueryWrapper<CvElectricityFactor>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "division_code", req.getRecordCode())
            .like(StringUtils.isNotBlank(req.getRecordName()), "division_name", req.getRecordName())
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvElectricityFactor> page = electricityMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "ef-electricity-factor", e.getDivisionCode(), e.getDivisionName(), null);
            vo.setFactorVersion(e.getFactorVersion());
            vo.setDivisionCode(e.getDivisionCode());
            vo.setDivisionName(e.getDivisionName());
            vo.setRegionName(e.getRegionName());
            vo.setProvinceFactor(e.getProvinceFactor());
            vo.setRegionFactor(e.getRegionFactor());
            vo.setNationalFactor(e.getNationalFactor());
            vo.setNonFossilExcludedFactor(e.getNonFossilExcludedFactor());
            vo.setNationalFossilPowerFactor(e.getNationalFossilPowerFactor());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    private QueryResult queryElectricityFactorVersion(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvElectricityFactorVersion> qw = new QueryWrapper<CvElectricityFactorVersion>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "factor_version", req.getRecordCode())
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvElectricityFactorVersion> page = electricityFactorVersionMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "ef-electricity-version", e.getFactorVersion(), e.getFactorVersion(), null);
            vo.setFactorVersion(e.getFactorVersion());
            vo.setEffectiveYear(e.getEffectiveYear());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    private QueryResult queryElectricityFactorScope(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvElectricityFactorScope> qw = new QueryWrapper<CvElectricityFactorScope>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "scope_key", req.getRecordCode())
            .like(StringUtils.isNotBlank(req.getRecordName()), "scope_name", req.getRecordName())
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvElectricityFactorScope> page = electricityFactorScopeMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "ef-electricity-scope", e.getScopeKey(), e.getScopeName(), null);
            vo.setScopeKey(e.getScopeKey());
            vo.setScopeName(e.getScopeName());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    private QueryResult queryGreenhouseGas(CvOpenDimensionRequest req, long pageNum, long pageSize) {
        QueryWrapper<CvGreenhouseGas> qw = new QueryWrapper<CvGreenhouseGas>()
            .eq("status", "0")
            .like(StringUtils.isNotBlank(req.getRecordCode()), "gas_code", req.getRecordCode())
            .like(StringUtils.isNotBlank(req.getRecordName()), "gas_name", req.getRecordName())
            .orderByAsc("sort_order").orderByAsc("id");
        Page<CvGreenhouseGas> page = greenhouseGasMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<CvOpenDimensionRecordVo> records = page.getRecords().stream().map(e -> {
            CvOpenDimensionRecordVo vo = baseVo(e.getId(), "greenhouse-gas", e.getGasCode(), e.getGasName(), null);
            vo.setGasCode(e.getGasCode());
            vo.setGasName(e.getGasName());
            vo.setGasNameEn(e.getGasNameEn());
            vo.setGwpValue(e.getGwpValue());
            vo.setGwpVersion(e.getGwpVersion());
            vo.setChemicalFormula(e.getChemicalFormula());
            vo.setSortOrder(e.getSortOrder());
            vo.setStatus(e.getStatus());
            vo.setCreateTime(e.getCreateTime());
            vo.setUpdateTime(e.getUpdateTime());
            vo.setRemark(e.getRemark());
            return vo;
        }).toList();
        return new QueryResult(records, page.getTotal());
    }

    // ==================== Helpers ====================

    private CvOpenDimensionRecordVo baseVo(Long id, String dimensionCode, String recordCode, String recordName, String parentCode) {
        CvOpenDimensionRecordVo vo = new CvOpenDimensionRecordVo();
        vo.setId(id);
        vo.setDimensionCode(dimensionCode);
        vo.setRecordCode(recordCode);
        vo.setRecordName(recordName);
        vo.setParentCode(parentCode);
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
