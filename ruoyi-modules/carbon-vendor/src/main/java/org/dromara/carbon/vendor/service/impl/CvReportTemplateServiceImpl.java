package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateBo;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateVo;
import org.dromara.carbon.vendor.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.service.ICvReportTemplateService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        CvReportTemplate reportTemplate = MapstructUtils.convert(bo, CvReportTemplate.class);
        return baseMapper.insert(reportTemplate);
    }

    @Override
    public int updateReportTemplate(CvReportTemplateBo bo) {
        CvReportTemplate reportTemplate = MapstructUtils.convert(bo, CvReportTemplate.class);
        return baseMapper.updateById(reportTemplate);
    }

    @Override
    public int deleteReportTemplateByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
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
}
