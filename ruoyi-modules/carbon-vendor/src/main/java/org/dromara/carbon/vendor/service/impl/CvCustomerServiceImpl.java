package org.dromara.carbon.vendor.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.bo.CvCustomerBo;
import org.dromara.carbon.vendor.domain.vo.CvCustomerVo;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.service.ICvCustomerService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Vendor customer archive service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvCustomerServiceImpl implements ICvCustomerService {

    private final CvCustomerMapper baseMapper;

    @Override
    public CvCustomerVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<CvCustomerVo> queryPageList(CvCustomerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvCustomer> lqw = buildQueryWrapper(bo);
        Page<CvCustomerVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<CvCustomerVo> queryList(CvCustomerBo bo) {
        LambdaQueryWrapper<CvCustomer> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CvCustomer> buildQueryWrapper(CvCustomerBo bo) {
        LambdaQueryWrapper<CvCustomer> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getCustomerCode()), CvCustomer::getCustomerCode, bo.getCustomerCode());
        lqw.like(StringUtils.isNotBlank(bo.getCustomerName()), CvCustomer::getCustomerName, bo.getCustomerName());
        lqw.like(StringUtils.isNotBlank(bo.getContactName()), CvCustomer::getContactName, bo.getContactName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustomerStatus()), CvCustomer::getCustomerStatus, bo.getCustomerStatus());
        lqw.orderByDesc(CvCustomer::getCreateTime).orderByDesc(CvCustomer::getId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(CvCustomerBo bo) {
        CvCustomer add = MapstructUtils.convert(bo, CvCustomer.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(CvCustomerBo bo) {
        CvCustomer update = MapstructUtils.convert(bo, CvCustomer.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public boolean checkCustomerCodeUnique(CvCustomerBo bo) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<CvCustomer>()
            .eq(CvCustomer::getCustomerCode, bo.getCustomerCode())
            .ne(ObjectUtil.isNotNull(bo.getId()), CvCustomer::getId, bo.getId()));
        return !exist;
    }
}
