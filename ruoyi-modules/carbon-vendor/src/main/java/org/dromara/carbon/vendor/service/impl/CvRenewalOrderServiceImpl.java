package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.bo.CvRenewalOrderBo;
import org.dromara.carbon.vendor.domain.vo.CvRenewalOrderVo;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.service.ICvRenewalOrderService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * Vendor renewal order service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvRenewalOrderServiceImpl implements ICvRenewalOrderService {

    private final CvRenewalOrderMapper baseMapper;

    @Override
    public TableDataInfo<CvRenewalOrderVo> selectPageRenewalOrderList(CvRenewalOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvRenewalOrder> lqw = buildQueryWrapper(bo);
        Page<CvRenewalOrderVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvRenewalOrderVo selectRenewalOrderById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertRenewalOrder(CvRenewalOrderBo bo) {
        CvRenewalOrder renewalOrder = MapstructUtils.convert(bo, CvRenewalOrder.class);
        return baseMapper.insert(renewalOrder);
    }

    @Override
    public int updateRenewalOrder(CvRenewalOrderBo bo) {
        CvRenewalOrder renewalOrder = MapstructUtils.convert(bo, CvRenewalOrder.class);
        return baseMapper.updateById(renewalOrder);
    }

    @Override
    public int deleteRenewalOrderByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<CvRenewalOrder> buildQueryWrapper(CvRenewalOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvRenewalOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvRenewalOrder::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), CvRenewalOrder::getOrderNo, bo.getOrderNo());
        lqw.eq(bo.getCustomerId() != null, CvRenewalOrder::getCustomerId, bo.getCustomerId());
        lqw.like(StringUtils.isNotBlank(bo.getLicenseId()), CvRenewalOrder::getLicenseId, bo.getLicenseId());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderStatus()), CvRenewalOrder::getOrderStatus, bo.getOrderStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getPayChannel()), CvRenewalOrder::getPayChannel, bo.getPayChannel());
        lqw.like(StringUtils.isNotBlank(bo.getIssuedLicenseId()), CvRenewalOrder::getIssuedLicenseId, bo.getIssuedLicenseId());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvRenewalOrder::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvRenewalOrder::getCreateTime);
        lqw.orderByAsc(CvRenewalOrder::getId);
        return lqw;
    }
}
