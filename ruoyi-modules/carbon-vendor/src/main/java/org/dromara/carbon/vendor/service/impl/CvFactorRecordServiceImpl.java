package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.bo.CvFactorRecordBo;
import org.dromara.carbon.vendor.domain.enums.CvFactorVersionLifecycleState;
import org.dromara.carbon.vendor.domain.vo.CvFactorRecordVo;
import org.dromara.carbon.vendor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.service.ICvFactorRecordService;
import org.dromara.common.core.exception.ServiceException;
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
    private final CvFactorVersionMapper factorVersionMapper;

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
        assertFactorVersionMutable(bo.getVersionId());
        normalizeSampleFields(bo);
        CvFactorRecord factorRecord = toEntity(bo);
        return baseMapper.insert(factorRecord);
    }

    @Override
    public int updateFactorRecord(CvFactorRecordBo bo) {
        CvFactorRecord existing = requireFactorRecord(bo.getId());
        assertFactorVersionMutable(existing.getVersionId());
        if (!existing.getVersionId().equals(bo.getVersionId())) {
            assertFactorVersionMutable(bo.getVersionId());
        }
        normalizeSampleFields(bo);
        CvFactorRecord factorRecord = toEntity(bo);
        return baseMapper.updateById(factorRecord);
    }

    @Override
    public int deleteFactorRecordByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        for (CvFactorRecord factorRecord : baseMapper.selectList(Wrappers.<CvFactorRecord>lambdaQuery()
            .in(CvFactorRecord::getId, Arrays.asList(ids)))) {
            assertFactorVersionMutable(factorRecord.getVersionId());
        }
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<CvFactorRecord> buildQueryWrapper(CvFactorRecordBo bo) {
        Map<String, Object> params = bo.getParams() == null ? Map.of() : bo.getParams();
        LambdaQueryWrapper<CvFactorRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, CvFactorRecord::getId, bo.getId());
        lqw.eq(bo.getVersionId() != null, CvFactorRecord::getVersionId, bo.getVersionId());
        lqw.eq(StringUtils.isNotBlank(bo.getFactorTableCode()), CvFactorRecord::getFactorTableCode, bo.getFactorTableCode());
        lqw.like(StringUtils.isNotBlank(bo.getFactorCode()), CvFactorRecord::getFactorCode, bo.getFactorCode());
        lqw.like(StringUtils.isNotBlank(bo.getFactorName()), CvFactorRecord::getFactorName, bo.getFactorName());
        lqw.eq(StringUtils.isNotBlank(bo.getFactorCategory()), CvFactorRecord::getFactorCategory, bo.getFactorCategory());
        lqw.like(StringUtils.isNotBlank(bo.getFactorKey()), CvFactorRecord::getFactorKey, bo.getFactorKey());
        lqw.like(StringUtils.isNotBlank(bo.getEmissionSourceName()), CvFactorRecord::getEmissionSourceName, bo.getEmissionSourceName());
        lqw.like(StringUtils.isNotBlank(bo.getFactorVersion()), CvFactorRecord::getFactorVersion, bo.getFactorVersion());
        lqw.eq(StringUtils.isNotBlank(bo.getFactorUnit()), CvFactorRecord::getFactorUnit, bo.getFactorUnit());
        lqw.eq(bo.getEnabledFlag() != null, CvFactorRecord::getEnabledFlag, bo.getEnabledFlag());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            CvFactorRecord::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByDesc(CvFactorRecord::getCreateTime);
        lqw.orderByAsc(CvFactorRecord::getId);
        return lqw;
    }

    private CvFactorRecord requireFactorRecord(Long id) {
        if (id == null) {
            throw new ServiceException("Factor record id cannot be null");
        }
        CvFactorRecord factorRecord = baseMapper.selectById(id);
        if (factorRecord == null) {
            throw new ServiceException("Factor record does not exist");
        }
        return factorRecord;
    }

    protected CvFactorRecord toEntity(CvFactorRecordBo bo) {
        return MapstructUtils.convert(bo, CvFactorRecord.class);
    }

    private void normalizeSampleFields(CvFactorRecordBo bo) {
        if (StringUtils.isBlank(bo.getFactorTableCode())) {
            bo.setFactorTableCode("201ef");
        }
        if (StringUtils.isBlank(bo.getFactorCode())) {
            bo.setFactorCode(firstText(
                bo.getFactorKey(),
                bo.getVersionProvinceCode(),
                bo.getFactorVersion(),
                bo.getFuelLevel4(),
                bo.getFuelLevel3(),
                bo.getFuelLevel2(),
                bo.getFuelLevel1()
            ));
        }
        if (StringUtils.isBlank(bo.getFactorName())) {
            bo.setFactorName(firstText(
                bo.getEmissionSourceName(),
                bo.getDivisionName(),
                bo.getFuelLevel4(),
                bo.getFuelLevel3(),
                bo.getFuelLevel2(),
                bo.getFuelLevel1(),
                bo.getFactorCode()
            ));
        }
        if (StringUtils.isBlank(bo.getFactorCategory())) {
            bo.setFactorCategory(firstText(bo.getFactorTableCode(), bo.getFuelMaterialCategory(), bo.getRegionName()));
        }
        if (bo.getFactorValue() == null) {
            bo.setFactorValue(firstNumber(
                bo.getFactorGwp(),
                bo.getProvinceFactor(),
                bo.getRegionFactor(),
                bo.getNationalFactor(),
                bo.getConvertedFactor(),
                bo.getCo2Factor(),
                bo.getCo2()
            ));
        }
        if (StringUtils.isBlank(bo.getFactorUnit())) {
            bo.setFactorUnit(firstText(bo.getSourceUnit(), "kgCO2e"));
        }
        if (StringUtils.isBlank(bo.getSourceRef())) {
            bo.setSourceRef(bo.getFactorSource());
        }
        if (bo.getEnabledFlag() == null) {
            bo.setEnabledFlag(Boolean.TRUE);
        }
    }

    private String firstText(String... values) {
        return Arrays.stream(values)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse("UNKNOWN");
    }

    private java.math.BigDecimal firstNumber(java.math.BigDecimal... values) {
        return Arrays.stream(values)
            .filter(value -> value != null)
            .findFirst()
            .orElse(java.math.BigDecimal.ZERO);
    }

    private void assertFactorVersionMutable(Long versionId) {
        if (versionId == null) {
            throw new ServiceException("Factor version id cannot be null");
        }
        CvFactorVersion version = factorVersionMapper.selectById(versionId);
        if (version == null) {
            throw new ServiceException("Factor version does not exist");
        }
        CvFactorVersionLifecycleState state = CvFactorVersionLifecycleState.fromVersion(version);
        if (state == CvFactorVersionLifecycleState.FROZEN || state == CvFactorVersionLifecycleState.RETIRED) {
            throw new ServiceException("Frozen or retired factor versions cannot modify core factor records");
        }
    }
}
