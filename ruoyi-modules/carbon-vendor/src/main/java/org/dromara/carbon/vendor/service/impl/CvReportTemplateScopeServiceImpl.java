package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.domain.enums.CvReportTemplateLifecycleState;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateScopeVo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.service.ICvReportTemplateScopeService;
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
    private final CvCustomerMapper customerMapper;
    private final CvLicenseIssueMapper licenseIssueMapper;
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
        reportTemplateScope.setLicenseId(normalizeLicenseId(reportTemplateScope.getLicenseId()));
        reportTemplateScope.setEdition(normalizeEdition(reportTemplateScope.getEdition()));
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
        reportTemplateScope.setLicenseId(normalizeLicenseId(reportTemplateScope.getLicenseId()));
        reportTemplateScope.setEdition(normalizeEdition(reportTemplateScope.getEdition()));
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
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvReportTemplateScope> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvReportTemplateScope::getId, bo.getId());
        lqw.eq(bo.getTemplateId() != null, CvReportTemplateScope::getTemplateId, bo.getTemplateId());
        lqw.eq(bo.getCustomerId() != null, CvReportTemplateScope::getCustomerId, bo.getCustomerId());
        lqw.eq(bo.getPackageId() != null, CvReportTemplateScope::getPackageId, bo.getPackageId());
        lqw.like(StringUtils.isNotBlank(bo.getPackageName()), CvReportTemplateScope::getPackageName, bo.getPackageName());
        lqw.eq(StringUtils.isNotBlank(bo.getEdition()), CvReportTemplateScope::getEdition, normalizeEdition(bo.getEdition()));
        lqw.like(StringUtils.isNotBlank(bo.getLicenseId()), CvReportTemplateScope::getLicenseId, bo.getLicenseId());
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
        if (StringUtils.isBlank(bo.getLicenseId()) && StringUtils.isBlank(bo.getEdition()) && bo.getPackageId() == null && bo.getCustomerId() == null) {
            throw new ServiceException("Distribution must reference customer, package, edition, or license entitlement metadata");
        }
        if (bo.getCustomerId() != null && customerMapper.selectById(bo.getCustomerId()) == null) {
            throw new ServiceException("Referenced vendor customer does not exist");
        }
        requireActivePackage(bo.getPackageId());
        String licenseId = normalizeLicenseId(bo.getLicenseId());
        if (licenseId != null && licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false) == null) {
            throw new ServiceException("Referenced vendor license entitlement does not exist");
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

    private void applyPackageSnapshot(CvReportTemplateScope scope) {
        SysTenantPackage tenantPackage = requireActivePackage(scope.getPackageId());
        if (tenantPackage != null) {
            scope.setPackageName(tenantPackage.getPackageName());
            scope.setEdition(null);
        } else {
            scope.setPackageName(null);
        }
    }

    private String normalizeLicenseId(String licenseId) {
        return StringUtils.isBlank(licenseId) ? null : licenseId.trim();
    }

    private String normalizeEdition(String edition) {
        return StringUtils.isBlank(edition) ? null : edition.trim().toLowerCase(Locale.ROOT);
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
        appendNullableEq(wrapper, CvReportTemplateScope::getCustomerId, scope.getCustomerId());
        appendNullableEq(wrapper, CvReportTemplateScope::getPackageId, scope.getPackageId());
        appendNullableEq(wrapper, CvReportTemplateScope::getEdition, scope.getEdition());
        appendNullableEq(wrapper, CvReportTemplateScope::getLicenseId, scope.getLicenseId());
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
