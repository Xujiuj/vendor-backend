package org.dromara.carbon.vendor.template.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.template.domain.CvReportTemplate;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.template.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.template.domain.enums.CvReportTemplateLifecycleState;
import org.dromara.carbon.vendor.template.domain.vo.CvReportTemplateScopeVo;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.template.service.ICvReportTemplateScopeService;
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
import java.util.Map;

/**
 * Vendor report template scope service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvReportTemplateScopeServiceImpl implements ICvReportTemplateScopeService {

    private static final String SCOPE_STATUS_ENABLED = "enabled";
    private static final String SCOPE_STATUS_DISABLED = "disabled";
    private static final String SCOPE_STATUS_DELETED = "deleted";

    private final CvReportTemplateScopeMapper baseMapper;
    private final CvReportTemplateMapper reportTemplateMapper;
    private final SysTenantPackageMapper tenantPackageMapper;

    @Override
    public TableDataInfo<CvReportTemplateScopeVo> selectPageReportTemplateScopeList(CvReportTemplateScopeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvReportTemplateScope> lqw = buildQueryWrapper(bo);
        Page<CvReportTemplateScopeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvReportTemplateScopeVo selectReportTemplateScopeById(Long id) {
        return baseMapper.selectVoOne(Wrappers.<CvReportTemplateScope>lambdaQuery()
            .eq(CvReportTemplateScope::getId, id)
            .ne(CvReportTemplateScope::getScopeStatus, SCOPE_STATUS_DELETED), false);
    }

    @Override
    public int insertReportTemplateScope(CvReportTemplateScopeBo bo) {
        validateScope(bo);
        CvReportTemplateScope reportTemplateScope = toEntity(bo);
        applyPackageSnapshot(reportTemplateScope);
        reportTemplateScope.setScopeStatus(normalizeScopeStatus(reportTemplateScope.getScopeStatus()));
        CvReportTemplateScope deletedScope = findDeletedScope(reportTemplateScope);
        if (deletedScope != null) {
            reportTemplateScope.setId(deletedScope.getId());
            return baseMapper.updateById(reportTemplateScope);
        }
        return baseMapper.insert(reportTemplateScope);
    }

    @Override
    public int updateReportTemplateScope(CvReportTemplateScopeBo bo) {
        validateScope(bo);
        CvReportTemplateScope reportTemplateScope = toEntity(bo);
        applyPackageSnapshot(reportTemplateScope);
        reportTemplateScope.setScopeStatus(normalizeScopeStatus(reportTemplateScope.getScopeStatus()));
        return baseMapper.updateById(reportTemplateScope);
    }

    @Override
    public int deleteReportTemplateScopeByIds(Long[] ids) {
        List<Long> idList = Arrays.stream(ids)
            .filter(id -> id != null)
            .toList();
        if (idList.isEmpty()) {
            return 0;
        }
        List<CvReportTemplateScope> scopes = baseMapper.selectList(Wrappers.<CvReportTemplateScope>lambdaQuery()
            .in(CvReportTemplateScope::getId, idList)
            .ne(CvReportTemplateScope::getScopeStatus, SCOPE_STATUS_DELETED));
        if (scopes.isEmpty()) {
            return 0;
        }
        scopes.forEach(scope -> scope.setScopeStatus(SCOPE_STATUS_DELETED));
        return baseMapper.updateBatchById(scopes) ? scopes.size() : 0;
    }

    private LambdaQueryWrapper<CvReportTemplateScope> buildQueryWrapper(CvReportTemplateScopeBo bo) {
        Map<String, Object> params = bo.getParams() == null ? Map.of() : bo.getParams();
        LambdaQueryWrapper<CvReportTemplateScope> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvReportTemplateScope::getId, bo.getId());
        lqw.eq(bo.getTemplateId() != null, CvReportTemplateScope::getTemplateId, bo.getTemplateId());
        lqw.eq(bo.getPackageId() != null, CvReportTemplateScope::getPackageId, bo.getPackageId());
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), CvReportTemplateScope::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getScopeStatus()), CvReportTemplateScope::getScopeStatus, bo.getScopeStatus());
        lqw.ne(CvReportTemplateScope::getScopeStatus, SCOPE_STATUS_DELETED);
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvReportTemplateScope::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvReportTemplateScope::getCreateTime);
        lqw.orderByAsc(CvReportTemplateScope::getId);
        return lqw;
    }

    private void validateScope(CvReportTemplateScopeBo bo) {
        if (bo.getTemplateId() == null) {
            throw new ServiceException("Report template distribution templateId cannot be null");
        }
        CvReportTemplate template = reportTemplateMapper.selectById(bo.getTemplateId());
        if (template == null || CvReportTemplateLifecycleState.fromTemplate(template) == CvReportTemplateLifecycleState.DELETED) {
            throw new ServiceException("Referenced report template does not exist");
        }
        if (CvReportTemplateLifecycleState.fromTemplate(template) != CvReportTemplateLifecycleState.PUBLISHED) {
            throw new ServiceException("Only published report templates can be distributed");
        }
        if (bo.getPackageId() == null) {
            throw new ServiceException("Distribution must reference a package");
        }
        requireActivePackage(bo.getPackageId());
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

    private void applyPackageSnapshot(CvReportTemplateScope scope) {
        SysTenantPackage tenantPackage = requireActivePackage(scope.getPackageId());
        if (tenantPackage != null) {
            scope.setPackageName(tenantPackage.getPackageName());
        } else {
            scope.setPackageName(null);
        }
    }

    private String normalizeScopeStatus(String scopeStatus) {
        String normalized = StringUtils.isBlank(scopeStatus) ? SCOPE_STATUS_ENABLED : scopeStatus.trim().toLowerCase();
        if (!SCOPE_STATUS_ENABLED.equals(normalized) && !SCOPE_STATUS_DISABLED.equals(normalized)) {
            throw new ServiceException("Unsupported report template distribution status");
        }
        return normalized;
    }

    private CvReportTemplateScope findDeletedScope(CvReportTemplateScope scope) {
        LambdaQueryWrapper<CvReportTemplateScope> wrapper = Wrappers.<CvReportTemplateScope>lambdaQuery()
            .eq(CvReportTemplateScope::getTemplateId, scope.getTemplateId())
            .eq(CvReportTemplateScope::getScopeStatus, SCOPE_STATUS_DELETED);
        appendNullableEq(wrapper, CvReportTemplateScope::getPackageId, scope.getPackageId());
        return baseMapper.selectOne(wrapper, false);
    }

    private <T> void appendNullableEq(LambdaQueryWrapper<CvReportTemplateScope> wrapper,
                                      SFunction<CvReportTemplateScope, T> column,
                                      T value) {
        if (value == null) {
            wrapper.isNull(column);
        } else {
            wrapper.eq(column, value);
        }
    }

    protected CvReportTemplateScope toEntity(CvReportTemplateScopeBo bo) {
        return MapstructUtils.convert(bo, CvReportTemplateScope.class);
    }
}
