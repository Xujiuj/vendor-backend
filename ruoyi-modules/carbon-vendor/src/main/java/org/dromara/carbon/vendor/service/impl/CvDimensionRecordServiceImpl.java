package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvDimensionRecord;
import org.dromara.carbon.vendor.domain.bo.CvDimensionRecordBo;
import org.dromara.carbon.vendor.domain.vo.CvDimensionRecordVo;
import org.dromara.carbon.vendor.mapper.CvDimensionRecordMapper;
import org.dromara.carbon.vendor.service.ICvDimensionRecordService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Vendor dimension record service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvDimensionRecordServiceImpl implements ICvDimensionRecordService {

    private static final Set<String> ALLOWED_DIMENSION_CODES = Set.of(
        "admin-division",
        "company",
        "emission-source-category",
        "base-year",
        "ef-electricity-version",
        "ef-electricity-scope",
        "greenhouse-gas",
        "intensity-denominator",
        "intensity-target",
        "intensity-tolerance",
        "report-template-download"
    );

    private final CvDimensionRecordMapper dimensionRecordMapper;

    @Override
    public TableDataInfo<CvDimensionRecordVo> queryPageList(CvDimensionRecordBo bo, PageQuery pageQuery) {
        validateDimensionCode(bo.getDimensionCode());
        IPage<CvDimensionRecordVo> page = dimensionRecordMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<CvDimensionRecordVo> queryList(CvDimensionRecordBo bo) {
        validateDimensionCode(bo.getDimensionCode());
        return dimensionRecordMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public CvDimensionRecordVo queryById(Long id) {
        return dimensionRecordMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(CvDimensionRecordBo bo) {
        validateDimensionCode(bo.getDimensionCode());
        normalizeDefaults(bo);
        CvDimensionRecord add = MapstructUtils.convert(bo, CvDimensionRecord.class);
        boolean flag = dimensionRecordMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(CvDimensionRecordBo bo) {
        validateDimensionCode(bo.getDimensionCode());
        normalizeDefaults(bo);
        CvDimensionRecord update = MapstructUtils.convert(bo, CvDimensionRecord.class);
        return dimensionRecordMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        return dimensionRecordMapper.deleteByIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<CvDimensionRecord> buildQueryWrapper(CvDimensionRecordBo bo) {
        return new LambdaQueryWrapper<CvDimensionRecord>()
            .eq(StringUtils.isNotBlank(bo.getDimensionCode()), CvDimensionRecord::getDimensionCode, bo.getDimensionCode())
            .like(StringUtils.isNotBlank(bo.getRecordCode()), CvDimensionRecord::getRecordCode, bo.getRecordCode())
            .like(StringUtils.isNotBlank(bo.getRecordName()), CvDimensionRecord::getRecordName, bo.getRecordName())
            .eq(StringUtils.isNotBlank(bo.getParentCode()), CvDimensionRecord::getParentCode, bo.getParentCode())
            .eq(StringUtils.isNotBlank(bo.getStatus()), CvDimensionRecord::getStatus, bo.getStatus())
            .orderByAsc(CvDimensionRecord::getSortOrder)
            .orderByAsc(CvDimensionRecord::getId);
    }

    private void normalizeDefaults(CvDimensionRecordBo bo) {
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus("0");
        }
        if (bo.getSortOrder() == null) {
            bo.setSortOrder(0);
        }
    }

    private void validateDimensionCode(String dimensionCode) {
        if (StringUtils.isBlank(dimensionCode)) {
            return;
        }
        if (!ALLOWED_DIMENSION_CODES.contains(dimensionCode)) {
            throw new ServiceException("Unsupported vendor dimension code: " + dimensionCode);
        }
    }
}
