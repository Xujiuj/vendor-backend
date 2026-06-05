package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateBo;
import org.dromara.carbon.vendor.domain.enums.CvReportTemplateLifecycleState;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateVo;
import org.dromara.carbon.vendor.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.service.ICvReportTemplateService;
import org.dromara.common.core.enums.FormatsType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Vendor report template service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvReportTemplateServiceImpl implements ICvReportTemplateService {

    private final CvReportTemplateMapper baseMapper;

    @Override
    public TableDataInfo<CvReportTemplateVo> selectPageReportTemplateList(CvReportTemplateBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvReportTemplate> lqw = buildQueryWrapper(bo);
        Page<CvReportTemplateVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvReportTemplateVo selectReportTemplateById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertReportTemplate(CvReportTemplateBo bo) {
        CvReportTemplate reportTemplate = toEntity(bo);
        reportTemplate.setPublishStatus(CvReportTemplateLifecycleState.DRAFT.getStatus());
        reportTemplate.setPublishedBy(null);
        reportTemplate.setPublishedTime(null);
        return baseMapper.insert(reportTemplate);
    }

    @Override
    public int updateReportTemplate(CvReportTemplateBo bo) {
        CvReportTemplate reportTemplate = toEntity(bo);
        preserveLifecycleMetadata(reportTemplate);
        return baseMapper.updateById(reportTemplate);
    }

    @Override
    public int deleteReportTemplateByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    @Override
    public void publishReportTemplate(Long id, String operatedBy) {
        CvReportTemplate template = requireReportTemplate(id);
        CvReportTemplateLifecycleState currentState = CvReportTemplateLifecycleState.fromTemplate(template);
        if (currentState == CvReportTemplateLifecycleState.PUBLISHED) {
            throw new ServiceException("Only draft or disabled report templates can be published");
        }
        Date operationTime = DateUtils.getNowDate();
        template.setPublishStatus(CvReportTemplateLifecycleState.PUBLISHED.getStatus());
        template.setPublishedBy(requireOperator(operatedBy));
        template.setPublishedTime(operationTime);
        template.setRemark(appendAuditRemark(template.getRemark(), "publish", operatedBy, operationTime));
        baseMapper.updateById(template);
    }

    @Override
    public void disableReportTemplate(Long id, String operatedBy) {
        CvReportTemplate template = requireReportTemplate(id);
        CvReportTemplateLifecycleState currentState = CvReportTemplateLifecycleState.fromTemplate(template);
        if (currentState != CvReportTemplateLifecycleState.PUBLISHED) {
            throw new ServiceException("Only published report templates can be disabled");
        }
        Date operationTime = DateUtils.getNowDate();
        template.setPublishStatus(CvReportTemplateLifecycleState.DISABLED.getStatus());
        template.setRemark(appendAuditRemark(template.getRemark(), "disable", operatedBy, operationTime));
        baseMapper.updateById(template);
    }

    private LambdaQueryWrapper<CvReportTemplate> buildQueryWrapper(CvReportTemplateBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvReportTemplate> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvReportTemplate::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getTemplateCode()), CvReportTemplate::getTemplateCode, bo.getTemplateCode());
        lqw.like(StringUtils.isNotBlank(bo.getTemplateName()), CvReportTemplate::getTemplateName, bo.getTemplateName());
        lqw.eq(StringUtils.isNotBlank(bo.getTemplateVersion()), CvReportTemplate::getTemplateVersion, bo.getTemplateVersion());
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), CvReportTemplate::getFileName, bo.getFileName());
        lqw.eq(StringUtils.isNotBlank(bo.getPublishStatus()), CvReportTemplate::getPublishStatus, bo.getPublishStatus());
        lqw.like(StringUtils.isNotBlank(bo.getPublishedBy()), CvReportTemplate::getPublishedBy, bo.getPublishedBy());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvReportTemplate::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvReportTemplate::getCreateTime);
        lqw.orderByAsc(CvReportTemplate::getId);
        return lqw;
    }

    private CvReportTemplate requireReportTemplate(Long id) {
        if (id == null) {
            throw new ServiceException("Report template id cannot be null");
        }
        CvReportTemplate template = baseMapper.selectById(id);
        if (template == null) {
            throw new ServiceException("Report template does not exist");
        }
        return template;
    }

    private String requireOperator(String operatedBy) {
        if (StringUtils.isBlank(operatedBy)) {
            throw new ServiceException("Report template lifecycle operator cannot be blank");
        }
        return operatedBy.trim();
    }

    private String appendAuditRemark(String currentRemark, String action, String operatedBy, Date operationTime) {
        String auditEntry = String.format(
            "[%s] report-template-%s by %s",
            DateUtils.parseDateToStr(FormatsType.YYYY_MM_DD_HH_MM_SS, operationTime),
            action,
            requireOperator(operatedBy)
        );
        return StringUtils.isBlank(currentRemark) ? auditEntry : currentRemark + System.lineSeparator() + auditEntry;
    }

    protected CvReportTemplate toEntity(CvReportTemplateBo bo) {
        return MapstructUtils.convert(bo, CvReportTemplate.class);
    }

    private void preserveLifecycleMetadata(CvReportTemplate reportTemplate) {
        CvReportTemplate existing = requireReportTemplate(reportTemplate.getId());
        reportTemplate.setPublishStatus(existing.getPublishStatus());
        reportTemplate.setPublishedBy(existing.getPublishedBy());
        reportTemplate.setPublishedTime(existing.getPublishedTime());
    }
}
