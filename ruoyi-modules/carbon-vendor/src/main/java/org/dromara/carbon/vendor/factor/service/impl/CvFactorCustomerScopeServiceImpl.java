package org.dromara.carbon.vendor.factor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.factor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.factor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.factor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.carbon.vendor.factor.mapper.CvFactorCustomerScopeMapper;
import org.dromara.carbon.vendor.factor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.factor.service.ICvFactorCustomerScopeService;
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

    private static final String SCOPE_STATUS_ENABLED = "enabled";
    private static final String SCOPE_STATUS_DISABLED = "disabled";
    private static final String SCOPE_STATUS_DELETED = "deleted";

    private final CvFactorCustomerScopeMapper baseMapper;
    private final CvFactorVersionMapper factorVersionMapper;
    private final SysTenantPackageMapper tenantPackageMapper;

    @Override
    public TableDataInfo<CvFactorCustomerScopeVo> selectPageFactorCustomerScopeList(CvFactorCustomerScopeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvFactorCustomerScope> lqw = buildQueryWrapper(bo);
        Page<CvFactorCustomerScopeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvFactorCustomerScopeVo selectFactorCustomerScopeById(Long id) {
        return baseMapper.selectVoOne(Wrappers.<CvFactorCustomerScope>lambdaQuery()
            .eq(CvFactorCustomerScope::getId, id)
            .ne(CvFactorCustomerScope::getScopeStatus, SCOPE_STATUS_DELETED), false);
    }

    @Override
    public int insertFactorCustomerScope(CvFactorCustomerScopeBo bo) {
        validateScope(bo);
        CvFactorCustomerScope factorCustomerScope = toEntity(bo);
        applyPackageSnapshot(factorCustomerScope);
        factorCustomerScope.setScopeStatus(normalizeScopeStatus(factorCustomerScope.getScopeStatus()));
        CvFactorCustomerScope deletedScope = findDeletedScope(factorCustomerScope);
        if (deletedScope != null) {
            factorCustomerScope.setId(deletedScope.getId());
            return baseMapper.updateById(factorCustomerScope);
        }
        return baseMapper.insert(factorCustomerScope);
    }

    @Override
    public int updateFactorCustomerScope(CvFactorCustomerScopeBo bo) {
        validateScope(bo);
        CvFactorCustomerScope factorCustomerScope = toEntity(bo);
        applyPackageSnapshot(factorCustomerScope);
        factorCustomerScope.setScopeStatus(normalizeScopeStatus(factorCustomerScope.getScopeStatus()));
        return baseMapper.updateById(factorCustomerScope);
    }

    @Override
    public boolean isFactorVersionAuthorized(Long versionId, Long packageId) {
        CvFactorVersion version = requirePublishedOrFrozenVersion(versionId);
        if (packageId == null) {
            return false;
        }
        return baseMapper.selectCount(Wrappers.<CvFactorCustomerScope>lambdaQuery()
            .eq(CvFactorCustomerScope::getVersionId, version.getId())
            .eq(CvFactorCustomerScope::getPackageId, packageId)
            .eq(CvFactorCustomerScope::getScopeStatus, SCOPE_STATUS_ENABLED)) > 0;
    }

    @Override
    public int deleteFactorCustomerScopeByIds(Long[] ids) {
        List<Long> idList = Arrays.stream(ids)
            .filter(id -> id != null)
            .toList();
        if (idList.isEmpty()) {
            return 0;
        }
        List<CvFactorCustomerScope> scopes = baseMapper.selectList(Wrappers.<CvFactorCustomerScope>lambdaQuery()
            .in(CvFactorCustomerScope::getId, idList)
            .ne(CvFactorCustomerScope::getScopeStatus, SCOPE_STATUS_DELETED));
        if (scopes.isEmpty()) {
            return 0;
        }
        scopes.forEach(scope -> scope.setScopeStatus(SCOPE_STATUS_DELETED));
        return baseMapper.updateBatchById(scopes) ? scopes.size() : 0;
    }

    private LambdaQueryWrapper<CvFactorCustomerScope> buildQueryWrapper(CvFactorCustomerScopeBo bo) {
        Map<String, Object> params = bo.getParams() == null ? Map.of() : bo.getParams();
        LambdaQueryWrapper<CvFactorCustomerScope> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorCustomerScope::getId, bo.getId());
        lqw.eq(bo.getVersionId() != null, CvFactorCustomerScope::getVersionId, bo.getVersionId());
        lqw.eq(bo.getPackageId() != null, CvFactorCustomerScope::getPackageId, bo.getPackageId());
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), CvFactorCustomerScope::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getScopeStatus()), CvFactorCustomerScope::getScopeStatus, normalizeScopeStatusFilter(bo.getScopeStatus()));
        lqw.ne(CvFactorCustomerScope::getScopeStatus, SCOPE_STATUS_DELETED);
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorCustomerScope::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorCustomerScope::getCreateTime);
        lqw.orderByAsc(CvFactorCustomerScope::getId);
        return lqw;
    }

    private void validateScope(CvFactorCustomerScopeBo bo) {
        requirePublishedOrFrozenVersion(bo.getVersionId());
        if (bo.getPackageId() == null) {
            throw new ServiceException("Factor scope must reference a package");
        }
        requireActivePackage(bo.getPackageId());
        if (hasDuplicateScope(bo)) {
            throw new ServiceException("Factor scope already exists for this version and package");
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

    private boolean hasDuplicateScope(CvFactorCustomerScopeBo bo) {
        LambdaQueryWrapper<CvFactorCustomerScope> query = Wrappers.lambdaQuery();
        query.eq(CvFactorCustomerScope::getVersionId, bo.getVersionId());
        query.eq(CvFactorCustomerScope::getPackageId, bo.getPackageId());
        query.ne(CvFactorCustomerScope::getScopeStatus, SCOPE_STATUS_DELETED);
        query.ne(bo.getId() != null, CvFactorCustomerScope::getId, bo.getId());
        return baseMapper.selectCount(query) > 0;
    }

    private String normalizeScopeStatus(String scopeStatus) {
        String normalized = StringUtils.isBlank(scopeStatus) ? SCOPE_STATUS_ENABLED : scopeStatus.trim().toLowerCase(Locale.ROOT);
        if (!SCOPE_STATUS_ENABLED.equals(normalized) && !SCOPE_STATUS_DISABLED.equals(normalized)) {
            throw new ServiceException("Unsupported factor scope status");
        }
        return normalized;
    }

    private String normalizeScopeStatusFilter(String scopeStatus) {
        String normalized = StringUtils.isBlank(scopeStatus) ? null : scopeStatus.trim().toLowerCase(Locale.ROOT);
        if (normalized == null || SCOPE_STATUS_ENABLED.equals(normalized) || SCOPE_STATUS_DISABLED.equals(normalized)) {
            return normalized;
        }
        return "__invalid_scope_status__";
    }

    private CvFactorCustomerScope findDeletedScope(CvFactorCustomerScope scope) {
        LambdaQueryWrapper<CvFactorCustomerScope> wrapper = Wrappers.<CvFactorCustomerScope>lambdaQuery()
            .eq(CvFactorCustomerScope::getVersionId, scope.getVersionId())
            .eq(CvFactorCustomerScope::getScopeStatus, SCOPE_STATUS_DELETED);
        appendNullableEq(wrapper, CvFactorCustomerScope::getPackageId, scope.getPackageId());
        return baseMapper.selectOne(wrapper, false);
    }

    private <T> void appendNullableEq(LambdaQueryWrapper<CvFactorCustomerScope> wrapper,
                                      SFunction<CvFactorCustomerScope, T> column,
                                      T value) {
        if (value == null) {
            wrapper.isNull(column);
        } else {
            wrapper.eq(column, value);
        }
    }

    protected CvFactorCustomerScope toEntity(CvFactorCustomerScopeBo bo) {
        return MapstructUtils.convert(bo, CvFactorCustomerScope.class);
    }
}
