package org.dromara.carbon.vendor.dimension.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.dimension.domain.CvDimensionRecord;
import org.dromara.carbon.vendor.dimension.domain.bo.CvDimensionRecordBo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvDimensionRecordVo;
import org.dromara.carbon.vendor.dimension.mapper.CvDimensionRecordMapper;
import org.dromara.carbon.vendor.dimension.service.ICvDimensionRecordService;
import org.dromara.carbon.vendor.shared.VendorManagedTableCatalog;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class CvDimensionRecordServiceImpl implements ICvDimensionRecordService {

    private static final String ENABLED = "0";

    private final CvDimensionRecordMapper baseMapper;

    @Override
    public TableDataInfo<CvDimensionRecordVo> queryPageList(CvDimensionRecordBo bo, PageQuery pageQuery) {
        IPage<CvDimensionRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public CvDimensionRecordVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public boolean insertByBo(CvDimensionRecordBo bo) {
        validateDimensionCode(bo.getDimensionCode());
        CvDimensionRecord entity = copyToEntity(bo);
        entity.setId(null);
        entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? ENABLED : entity.getStatus());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(CvDimensionRecordBo bo) {
        validateDimensionCode(bo.getDimensionCode());
        CvDimensionRecord entity = copyToEntity(bo);
        entity.setUpdateTime(new Date());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(Long[] ids) {
        return baseMapper.deleteByIds(java.util.List.of(ids)) > 0;
    }

    private LambdaQueryWrapper<CvDimensionRecord> buildQueryWrapper(CvDimensionRecordBo bo) {
        LambdaQueryWrapper<CvDimensionRecord> wrapper = Wrappers.lambdaQuery();
        if (bo == null) {
            return wrapper.orderByAsc(CvDimensionRecord::getDimensionCode)
                .orderByAsc(CvDimensionRecord::getSortOrder)
                .orderByAsc(CvDimensionRecord::getId);
        }
        wrapper.eq(StringUtils.isNotBlank(bo.getDimensionCode()), CvDimensionRecord::getDimensionCode, bo.getDimensionCode())
            .like(StringUtils.isNotBlank(bo.getRecordCode()), CvDimensionRecord::getRecordCode, bo.getRecordCode())
            .like(StringUtils.isNotBlank(bo.getRecordName()), CvDimensionRecord::getRecordName, bo.getRecordName())
            .eq(StringUtils.isNotBlank(bo.getParentCode()), CvDimensionRecord::getParentCode, bo.getParentCode())
            .eq(StringUtils.isNotBlank(bo.getStatus()), CvDimensionRecord::getStatus, bo.getStatus())
            .orderByAsc(CvDimensionRecord::getDimensionCode)
            .orderByAsc(CvDimensionRecord::getSortOrder)
            .orderByAsc(CvDimensionRecord::getId);
        return wrapper;
    }

    private void validateDimensionCode(String dimensionCode) {
        if (StringUtils.isBlank(dimensionCode)) {
            throw new ServiceException("dimensionCode cannot be blank");
        }
        if (!VendorManagedTableCatalog.isStrongDimensionCode(dimensionCode)) {
            throw new ServiceException("unsupported vendor dimensionCode: " + dimensionCode);
        }
    }

    private CvDimensionRecord copyToEntity(CvDimensionRecordBo bo) {
        CvDimensionRecord entity = new CvDimensionRecord();
        entity.setId(bo.getId());
        entity.setDimensionCode(bo.getDimensionCode());
        entity.setRecordCode(bo.getRecordCode());
        entity.setRecordName(bo.getRecordName());
        entity.setParentCode(bo.getParentCode());
        entity.setField01(bo.getField01());
        entity.setField02(bo.getField02());
        entity.setField03(bo.getField03());
        entity.setField04(bo.getField04());
        entity.setField05(bo.getField05());
        entity.setField06(bo.getField06());
        entity.setField07(bo.getField07());
        entity.setField08(bo.getField08());
        entity.setField09(bo.getField09());
        entity.setField10(bo.getField10());
        entity.setField11(bo.getField11());
        entity.setField12(bo.getField12());
        entity.setField13(bo.getField13());
        entity.setField14(bo.getField14());
        entity.setField15(bo.getField15());
        entity.setField16(bo.getField16());
        entity.setField17(bo.getField17());
        entity.setField18(bo.getField18());
        entity.setField19(bo.getField19());
        entity.setField20(bo.getField20());
        entity.setField21(bo.getField21());
        entity.setField22(bo.getField22());
        entity.setSortOrder(bo.getSortOrder());
        entity.setStatus(bo.getStatus());
        entity.setRemark(bo.getRemark());
        return entity;
    }
}
