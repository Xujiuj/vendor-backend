package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.bo.CvCustomerBo;
import org.dromara.carbon.vendor.domain.vo.CvCustomerVo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.service.ICvCustomerService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Vendor customer service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvCustomerServiceImpl implements ICvCustomerService {

    private final CvCustomerMapper baseMapper;

    @Override
    public TableDataInfo<CvCustomerVo> selectPageCustomerList(CvCustomerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvCustomer> lqw = buildQueryWrapper(bo);
        Page<CvCustomerVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvCustomerVo selectCustomerById(Long id) {
        return baseMapper.selectVoById(id);
    }

    private LambdaQueryWrapper<CvCustomer> buildQueryWrapper(CvCustomerBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvCustomer> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvCustomer::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerCode()), CvCustomer::getCustomerCode, bo.getCustomerCode());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), CvCustomer::getCustomerName, bo.getCustomerName());
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), CvCustomer::getContactName, bo.getContactName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerStatus()), CvCustomer::getCustomerStatus, bo.getCustomerStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvCustomer::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvCustomer::getCreateTime);
        lqw.orderByAsc(CvCustomer::getId);
        return lqw;
    }
}
