package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.bo.CvFactorRecordBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorRecordVo;
import org.dromara.carbon.vendor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.service.ICvFactorRecordService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

/**
 * Vendor factor record service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvFactorRecordServiceImpl implements ICvFactorRecordService {

    private final CvFactorRecordMapper baseMapper;

    @Override
    public TableDataInfo<CvFactorRecordVo> selectPageFactorRecordList(CvFactorRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CvFactorRecord> lqw = buildQueryWrapper(bo);
        Page<CvFactorRecordVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public CvFactorRecordVo selectFactorRecordById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public int insertFactorRecord(CvFactorRecordBo bo) {
        CvFactorRecord factorRecord = MapstructUtils.convert(bo, CvFactorRecord.class);
        return baseMapper.insert(factorRecord);
    }

    @Override
    public int updateFactorRecord(CvFactorRecordBo bo) {
        CvFactorRecord factorRecord = MapstructUtils.convert(bo, CvFactorRecord.class);
        return baseMapper.updateById(factorRecord);
    }

    @Override
    public int deleteFactorRecordByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<CvFactorRecord> buildQueryWrapper(CvFactorRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CvFactorRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorRecord::getId, bo.getId());
        lqw.eq(bo.getVersionId() != null, CvFactorRecord::getVersionId, bo.getVersionId());
        lqw.like(StringUtils.isNotBlank(bo.getFactorCode()), CvFactorRecord::getFactorCode, bo.getFactorCode());
        lqw.like(StringUtils.isNotBlank(bo.getFactorName()), CvFactorRecord::getFactorName, bo.getFactorName());
        lqw.eq(StringUtils.isNotBlank(bo.getFactorCategory()), CvFactorRecord::getFactorCategory, bo.getFactorCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getFactorUnit()), CvFactorRecord::getFactorUnit, bo.getFactorUnit());
        lqw.eq(bo.getEnabledFlag() != null, CvFactorRecord::getEnabledFlag, bo.getEnabledFlag());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorRecord::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorRecord::getCreateTime);
        lqw.orderByAsc(CvFactorRecord::getId);
        return lqw;
    }
}
