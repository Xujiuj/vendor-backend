package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import java.util.Locale;
import java.util.Map;

/**
 * Vendor report template scope service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvReportTemplateScopeServiceImpl implements ICvReportTemplateScopeService {

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
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertReportTemplateScope(CvReportTemplateScopeBo bo) {
        validateScope(bo);
        CvReportTemplateScope reportTemplateScope = toEntity(bo);
        applyPackageSnapshot(reportTemplateScope);
        reportTemplateScope.setLicenseId(normalizeLicenseId(reportTemplateScope.getLicenseId()));
        reportTemplateScope.setEdition(normalizeEdition(reportTemplateScope.getEdition()));
        reportTemplateScope.setScopeStatus(normalizeScopeStatus(reportTemplateScope.getScopeStatus()));
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
        return baseMapper.deleteByIds(Arrays.asList(ids));
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
        if (template == null) {
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
        String normalized = StringUtils.isBlank(scopeStatus) ? "enabled" : scopeStatus.trim().toLowerCase();
        if (!"enabled".equals(normalized) && !"disabled".equals(normalized)) {
            throw new ServiceException("Unsupported report template distribution status");
        }
        return normalized;
    }

    protected CvReportTemplateScope toEntity(CvReportTemplateScopeBo bo) {
        return MapstructUtils.convert(bo, CvReportTemplateScope.class);
    }
}
