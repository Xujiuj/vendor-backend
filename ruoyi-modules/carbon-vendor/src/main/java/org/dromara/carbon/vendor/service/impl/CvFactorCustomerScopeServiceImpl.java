package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.carbon.vendor.mapper.CvFactorCustomerScopeMapper;
import org.dromara.carbon.vendor.service.ICvFactorCustomerScopeService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * Vendor factor customer scope service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvFactorCustomerScopeServiceImpl implements ICvFactorCustomerScopeService {

    private final CvFactorCustomerScopeMapper baseMapper;

    @Override
    public TableDataInfo<CvFactorCustomerScopeVo> selectPageFactorCustomerScopeList(CvFactorCustomerScopeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvFactorCustomerScope> lqw = buildQueryWrapper(bo);
        Page<CvFactorCustomerScopeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvFactorCustomerScopeVo selectFactorCustomerScopeById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertFactorCustomerScope(CvFactorCustomerScopeBo bo) {
        CvFactorCustomerScope factorCustomerScope = MapstructUtils.convert(bo, CvFactorCustomerScope.class);
        return baseMapper.insert(factorCustomerScope);
    }

    @Override
    public int updateFactorCustomerScope(CvFactorCustomerScopeBo bo) {
        CvFactorCustomerScope factorCustomerScope = MapstructUtils.convert(bo, CvFactorCustomerScope.class);
        return baseMapper.updateById(factorCustomerScope);
    }

    @Override
    public int deleteFactorCustomerScopeByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<CvFactorCustomerScope> buildQueryWrapper(CvFactorCustomerScopeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvFactorCustomerScope> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorCustomerScope::getId, bo.getId());
        lqw.eq(bo.getVersionId() != null, CvFactorCustomerScope::getVersionId, bo.getVersionId());
        lqw.eq(bo.getCustomerId() != null, CvFactorCustomerScope::getCustomerId, bo.getCustomerId());
        lqw.like(StringUtils.isNotBlank(bo.getLicenseId()), CvFactorCustomerScope::getLicenseId, bo.getLicenseId());
        lqw.eq(StringUtils.isNotBlank(bo.getScopeStatus()), CvFactorCustomerScope::getScopeStatus, bo.getScopeStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorCustomerScope::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorCustomerScope::getCreateTime);
        lqw.orderByAsc(CvFactorCustomerScope::getId);
        return lqw;
    }
}
