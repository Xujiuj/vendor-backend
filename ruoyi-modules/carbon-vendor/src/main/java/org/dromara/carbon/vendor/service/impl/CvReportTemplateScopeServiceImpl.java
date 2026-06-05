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
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        reportTemplateScope.setLicenseId(normalizeLicenseId(reportTemplateScope.getLicenseId()));
        reportTemplateScope.setScopeStatus(normalizeScopeStatus(reportTemplateScope.getScopeStatus()));
        return baseMapper.insert(reportTemplateScope);
    }

    @Override
    public int updateReportTemplateScope(CvReportTemplateScopeBo bo) {
        validateScope(bo);
        CvReportTemplateScope reportTemplateScope = toEntity(bo);
        reportTemplateScope.setLicenseId(normalizeLicenseId(reportTemplateScope.getLicenseId()));
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
        if (StringUtils.isBlank(bo.getLicenseId()) && bo.getCustomerId() == null) {
            throw new ServiceException("Distribution must reference customer or license entitlement metadata");
        }
        if (bo.getCustomerId() != null && customerMapper.selectById(bo.getCustomerId()) == null) {
            throw new ServiceException("Referenced vendor customer does not exist");
        }
        String licenseId = normalizeLicenseId(bo.getLicenseId());
        if (licenseId != null && licenseIssueMapper.selectOne(Wrappers.<CvLicenseIssue>lambdaQuery()
            .eq(CvLicenseIssue::getLicenseId, licenseId), false) == null) {
            throw new ServiceException("Referenced vendor license entitlement does not exist");
        }
    }

    private String normalizeLicenseId(String licenseId) {
        return StringUtils.isBlank(licenseId) ? null : licenseId.trim();
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
