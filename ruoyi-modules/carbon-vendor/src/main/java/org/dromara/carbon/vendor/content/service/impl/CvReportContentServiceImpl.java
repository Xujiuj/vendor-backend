package org.dromara.carbon.vendor.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.content.domain.CvReportContent;
import org.dromara.carbon.vendor.content.domain.bo.CvReportContentBo;
import org.dromara.carbon.vendor.content.domain.vo.CvReportContentVo;
import org.dromara.carbon.vendor.content.mapper.CvReportContentMapper;
import org.dromara.carbon.vendor.content.service.ICvReportContentService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

/**
 * Vendor report content catalog service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvReportContentServiceImpl implements ICvReportContentService {

    private static final String STATUS_ENABLED = "0";

    private final CvReportContentMapper baseMapper;

    @Override
    public TableDataInfo<CvReportContentVo> selectPageReportContentList(CvReportContentBo bo, PageQuery pageQuery) {
        Page<CvReportContentVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public CvReportContentVo selectReportContentById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean insertReportContent(CvReportContentBo bo) {
        CvReportContent entity = toEntity(bo);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        boolean flag = baseMapper.insert(entity) > 0;
        if (flag) {
            bo.setId(entity.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateReportContent(CvReportContentBo bo) {
        CvReportContent entity = toEntity(bo);
        entity.setUpdateTime(new Date());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteReportContentByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        return baseMapper.deleteByIds(Arrays.asList(ids)) > 0;
    }

    private CvReportContent toEntity(CvReportContentBo bo) {
        CvReportContent entity = MapstructUtils.convert(bo, CvReportContent.class);
        entity.setDirectoryName(StringUtils.trim(entity.getDirectoryName()));
        entity.setSubdirectoryName(StringUtils.trim(entity.getSubdirectoryName()));
        entity.setChartNames(StringUtils.trim(entity.getChartNames()));
        entity.setStatus(StringUtils.blankToDefault(entity.getStatus(), STATUS_ENABLED));
        entity.setDisplayOrder(entity.getDisplayOrder() == null ? 0 : entity.getDisplayOrder());
        return entity;
    }

    private LambdaQueryWrapper<CvReportContent> buildQueryWrapper(CvReportContentBo bo) {
        LambdaQueryWrapper<CvReportContent> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getDirectoryNo() != null, CvReportContent::getDirectoryNo, bo.getDirectoryNo());
        lqw.like(StringUtils.isNotBlank(bo.getDirectoryName()), CvReportContent::getDirectoryName, bo.getDirectoryName());
        lqw.eq(bo.getSubdirectoryNo() != null, CvReportContent::getSubdirectoryNo, bo.getSubdirectoryNo());
        lqw.like(StringUtils.isNotBlank(bo.getSubdirectoryName()), CvReportContent::getSubdirectoryName, bo.getSubdirectoryName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), CvReportContent::getStatus, bo.getStatus());
        lqw.orderByAsc(CvReportContent::getDisplayOrder)
            .orderByAsc(CvReportContent::getDirectoryNo)
            .orderByAsc(CvReportContent::getSubdirectoryNo)
            .orderByAsc(CvReportContent::getId);
        return lqw;
    }
}
