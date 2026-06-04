package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorVersionVo;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.service.ICvFactorVersionService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Vendor factor version service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvFactorVersionServiceImpl implements ICvFactorVersionService {

    private final CvFactorVersionMapper baseMapper;

    @Override
    public TableDataInfo<CvFactorVersionVo> selectPageFactorVersionList(CvFactorVersionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvFactorVersion> lqw = buildQueryWrapper(bo);
        Page<CvFactorVersionVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvFactorVersionVo selectFactorVersionById(Long id) {
        return baseMapper.selectVoById(id);
    }

    private LambdaQueryWrapper<CvFactorVersion> buildQueryWrapper(CvFactorVersionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvFactorVersion> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorVersion::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getVersionCode()), CvFactorVersion::getVersionCode, bo.getVersionCode());
        lqw.like(StringUtils.isNotBlank(bo.getVersionName()), CvFactorVersion::getVersionName, bo.getVersionName());
        lqw.eq(StringUtils.isNotBlank(bo.getPublishStatus()), CvFactorVersion::getPublishStatus, bo.getPublishStatus());
        lqw.eq(bo.getFrozenFlag() != null, CvFactorVersion::getFrozenFlag, bo.getFrozenFlag());
        lqw.like(StringUtils.isNotBlank(bo.getPublishedBy()), CvFactorVersion::getPublishedBy, bo.getPublishedBy());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorVersion::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorVersion::getCreateTime);
        lqw.orderByAsc(CvFactorVersion::getId);
        return lqw;
    }
}
