package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateScopeVo;
import org.dromara.carbon.vendor.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.service.ICvReportTemplateScopeService;
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
        CvReportTemplateScope reportTemplateScope = MapstructUtils.convert(bo, CvReportTemplateScope.class);
        return baseMapper.insert(reportTemplateScope);
    }

    @Override
    public int updateReportTemplateScope(CvReportTemplateScopeBo bo) {
        CvReportTemplateScope reportTemplateScope = MapstructUtils.convert(bo, CvReportTemplateScope.class);
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
}
