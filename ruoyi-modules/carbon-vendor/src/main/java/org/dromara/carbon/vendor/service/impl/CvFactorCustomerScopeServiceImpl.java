package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvFactorCustomerScopeMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.service.ICvFactorCustomerScopeService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Vendor factor customer scope service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvFactorCustomerScopeServiceImpl implements ICvFactorCustomerScopeService {

    private static final String ISSUE_STATUS_REVOKED = "revoked";

    private final CvFactorCustomerScopeMapper baseMapper;
    private final CvFactorVersionMapper factorVersionMapper;
    private final CvCustomerMapper customerMapper;
    private final CvLicenseIssueMapper licenseIssueMapper;
    private final SysTenantPackageMapper tenantPackageMapper;

    @Override
    public TableDataInfo<CvFactorCustomerScopeVo> selectPageFactorCustomerScopeList(CvFactorCustomerScopeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvFactorCustomerScope> lqw = buildQueryWrapper(bo);
        Page<CvFactorCustomerScopeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvFactorCustomerScopeVo selectFactorCustomerScopeById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertFactorCustomerScope(CvFactorCustomerScopeBo bo) {
        validateScope(bo);
        CvFactorCustomerScope factorCustomerScope = toEntity(bo);
        applyPackageSnapshot(factorCustomerScope);
        factorCustomerScope.setEdition(normalizeEdition(factorCustomerScope.getEdition()));
        factorCustomerScope.setLicenseId(null);
        factorCustomerScope.setScopeStatus(normalizeScopeStatus(factorCustomerScope.getScopeStatus()));
        return baseMapper.insert(factorCustomerScope);
    }

    @Override
    public int updateFactorCustomerScope(CvFactorCustomerScopeBo bo) {
        validateScope(bo);
        CvFactorCustomerScope factorCustomerScope = toEntity(bo);
        applyPackageSnapshot(factorCustomerScope);
        factorCustomerScope.setEdition(normalizeEdition(factorCustomerScope.getEdition()));
        factorCustomerScope.setLicenseId(null);
        factorCustomerScope.setScopeStatus(normalizeScopeStatus(factorCustomerScope.getScopeStatus()));
        return baseMapper.updateById(factorCustomerScope);
    }

    @Override
    public boolean isFactorVersionAuthorized(Long versionId, Long customerId, Long packageId, String edition, String licenseId) {
        CvFactorVersion version = requirePublishedOrFrozenVersion(versionId);
        String normalizedEdition = normalizeEdition(edition);
        String normalizedLicenseId = normalizeLicenseId(licenseId);
        CvLicenseIssue entitlement = findLicenseIssue(normalizedLicenseId);
        if (entitlement == null) {
            return false;
        }
        if (isRevokedIssue(entitlement)) {
            return false;
        }
        Long resolvedCustomerId = entitlementCustomerId(entitlement);
        Long resolvedPackageId = entitlementPackageId(entitlement, packageId);
        String resolvedEdition = entitlementEdition(entitlement);
        if ((customerId != null && !customerId.equals(resolvedCustomerId))
            || (packageId != null && !packageId.equals(resolvedPackageId))
            || (normalizedEdition != null && !normalizedEdition.equals(resolvedEdition))) {
            return false;
        }
        if (resolvedCustomerId == null && resolvedPackageId == null && resolvedEdition == null) {
            return false;
        }
        List<CvFactorCustomerScope> scopes = baseMapper.selectList(Wrappers.<CvFactorCustomerScope>lambdaQuery()
            .eq(CvFactorCustomerScope::getVersionId, version.getId())
            .eq(CvFactorCustomerScope::getScopeStatus, "enabled"));
        if (scopes.isEmpty()) {
            return false;
        }
        return scopes.stream().anyMatch(scope -> matchesScope(scope, resolvedCustomerId, resolvedPackageId, resolvedEdition));
    }

    @Override
    public int deleteFactorCustomerScopeByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<CvFactorCustomerScope> buildQueryWrapper(CvFactorCustomerScopeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvFactorCustomerScope> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorCustomerScope::getId, bo.getId());
        lqw.eq(bo.getVersionId() != null, CvFactorCustomerScope::getVersionId, bo.getVersionId());
        lqw.eq(bo.getCustomerId() != null, CvFactorCustomerScope::getCustomerId, bo.getCustomerId());
        lqw.eq(bo.getPackageId() != null, CvFactorCustomerScope::getPackageId, bo.getPackageId());
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), CvFactorCustomerScope::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getEdition()), CvFactorCustomerScope::getEdition, normalizeEdition(bo.getEdition()));
        lqw.eq(StringUtils.isNotBlank(bo.getLicenseId()), CvFactorCustomerScope::getLicenseId, normalizeLicenseId(bo.getLicenseId()));
        lqw.eq(StringUtils.isNotBlank(bo.getScopeStatus()), CvFactorCustomerScope::getScopeStatus, normalizeScopeStatusFilter(bo.getScopeStatus()));
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorCustomerScope::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorCustomerScope::getCreateTime);
        lqw.orderByAsc(CvFactorCustomerScope::getId);
        return lqw;
    }

    private void validateScope(CvFactorCustomerScopeBo bo) {
        requirePublishedOrFrozenVersion(bo.getVersionId());
        String normalizedEdition = normalizeEdition(bo.getEdition());
        if (bo.getCustomerId() != null) {
            CvCustomer customer = customerMapper.selectById(bo.getCustomerId());
            if (customer == null) {
                throw new ServiceException("Referenced vendor customer does not exist");
            }
        }
        requireActivePackage(bo.getPackageId());
        if (bo.getPackageId() != null) {
            normalizedEdition = null;
        }
        if (hasDuplicateScope(bo, normalizedEdition)) {
            throw new ServiceException("Factor scope already exists for this version, customer, and package");
        }
    }

    private SysTenantPackage requireActivePackage(Long packageId) {
        if (packageId == null) {
            return null;
        }
        SysTenantPackage tenantPackage = tenantPackageMapper.selectById(packageId);
        if (tenantPackage == null || "1".equals(tenantPackage.getDelFlag())) {
            throw new ServiceException("Referenced package does not exist");
        }
        if (!"0".equals(tenantPackage.getStatus())) {
            throw new ServiceException("Referenced package is disabled");
        }
        return tenantPackage;
    }

    private void applyPackageSnapshot(CvFactorCustomerScope scope) {
        SysTenantPackage tenantPackage = requireActivePackage(scope.getPackageId());
        if (tenantPackage != null) {
            scope.setPackageName(tenantPackage.getPackageName());
            scope.setEdition(null);
        } else {
            scope.setPackageName(null);
        }
    }

    private CvFactorVersion requirePublishedOrFrozenVersion(Long versionId) {
        if (versionId == null) {
            throw new ServiceException("Factor version id cannot be null");
        }
        CvFactorVersion version = factorVersionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException("Factor version does not exist");
        }
        CvFactorVersionLifecycleState state = CvFactorVersionLifecycleState.fromVersion(version);
        if (state != CvFactorVersionLifecycleState.PUBLISHED && state != CvFactorVersionLifecycleState.FROZEN) {
            throw new ServiceException("Only published or frozen factor versions can be scoped");
        }
        return version;
    }

    private CvLicenseIssue findLicenseIssue(String licenseId) {
        if (licenseId == null) {
            return null;
        }
        return licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false);
    }

    private boolean isRevokedIssue(CvLicenseIssue issue) {
        return issue.getRevokedTime() != null || ISSUE_STATUS_REVOKED.equals(normalizeStatus(issue.getIssueStatus()));
    }

    private boolean hasDuplicateScope(CvFactorCustomerScopeBo bo, String normalizedEdition) {
        LambdaQueryWrapper<CvFactorCustomerScope> query = Wrappers.lambdaQuery();
        query.eq(CvFactorCustomerScope::getVersionId, bo.getVersionId());
        query.eq(bo.getCustomerId() != null, CvFactorCustomerScope::getCustomerId, bo.getCustomerId());
        query.isNull(bo.getCustomerId() == null, CvFactorCustomerScope::getCustomerId);
        query.eq(bo.getPackageId() != null, CvFactorCustomerScope::getPackageId, bo.getPackageId());
        query.isNull(bo.getPackageId() == null, CvFactorCustomerScope::getPackageId);
        query.eq(normalizedEdition != null, CvFactorCustomerScope::getEdition, normalizedEdition);
        query.isNull(normalizedEdition == null, CvFactorCustomerScope::getEdition);
        query.ne(bo.getId() != null, CvFactorCustomerScope::getId, bo.getId());
        return baseMapper.selectCount(query) > 0;
    }

    private boolean matchesScope(CvFactorCustomerScope scope, Long customerId, Long packageId, String edition) {
        return matchesCustomer(scope, customerId) && matchesPackage(scope, packageId) && matchesEdition(scope, edition);
    }

    private boolean matchesCustomer(CvFactorCustomerScope scope, Long customerId) {
        return scope.getCustomerId() == null || scope.getCustomerId().equals(customerId);
    }

    private boolean matchesEdition(CvFactorCustomerScope scope, String edition) {
        String scopeEdition = normalizeEdition(scope.getEdition());
        return scopeEdition == null || scopeEdition.equals(edition);
    }

    private boolean matchesPackage(CvFactorCustomerScope scope, Long packageId) {
        return scope.getPackageId() == null || scope.getPackageId().equals(packageId);
    }

    private Long entitlementCustomerId(CvLicenseIssue entitlement) {
        return entitlement == null ? null : entitlement.getCustomerId();
    }

    private String entitlementEdition(CvLicenseIssue entitlement) {
        return entitlement == null ? null : normalizeEdition(entitlement.getEdition());
    }

    private Long entitlementPackageId(CvLicenseIssue entitlement, Long requestPackageId) {
        if (entitlement != null && entitlement.getPackageId() != null) {
            return entitlement.getPackageId();
        }
        return requestPackageId;
    }

    private String normalizeEdition(String edition) {
        return StringUtils.isBlank(edition) ? null : edition.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        return StringUtils.isBlank(status) ? null : status.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLicenseId(String licenseId) {
        return StringUtils.isBlank(licenseId) ? null : licenseId.trim();
    }

    private String normalizeScopeStatus(String scopeStatus) {
        String normalized = StringUtils.isBlank(scopeStatus) ? "enabled" : scopeStatus.trim().toLowerCase(Locale.ROOT);
        if (!"enabled".equals(normalized) && !"disabled".equals(normalized)) {
            throw new ServiceException("Unsupported factor scope status");
        }
        return normalized;
    }

    private String normalizeScopeStatusFilter(String scopeStatus) {
        String normalized = StringUtils.isBlank(scopeStatus) ? null : scopeStatus.trim().toLowerCase(Locale.ROOT);
        if (normalized == null || "enabled".equals(normalized) || "disabled".equals(normalized)) {
            return normalized;
        }
        return "__invalid_scope_status__";
    }

    protected CvFactorCustomerScope toEntity(CvFactorCustomerScopeBo bo) {
        return MapstructUtils.convert(bo, CvFactorCustomerScope.class);
    }
}
