package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvVendorTableField;
import org.dromara.carbon.vendor.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.domain.vo.CvVendorTableFieldVo;
import org.dromara.carbon.vendor.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.service.ICvVendorTableFieldService;
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
 * Vendor table field definition service implementation.
 */
@RequiredArgsConstructor
@Service
public class CvVendorTableFieldServiceImpl implements ICvVendorTableFieldService {

    private static final Set<String> ALLOWED_TABLE_GROUPS = Set.of("factor", "dimension");

    private final CvVendorTableFieldMapper baseMapper;

    @Override
    public TableDataInfo<CvVendorTableFieldVo> queryPageList(CvVendorTableFieldBo bo, PageQuery pageQuery) {
        IPage<CvVendorTableFieldVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<CvVendorTableFieldVo> queryList(CvVendorTableFieldBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public CvVendorTableFieldVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(CvVendorTableFieldBo bo) {
        normalizeAndValidate(bo);
        ensureUnique(bo);
        CvVendorTableField add = MapstructUtils.convert(bo, CvVendorTableField.class);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(CvVendorTableFieldBo bo) {
        normalizeAndValidate(bo);
        ensureUnique(bo);
        CvVendorTableField update = MapstructUtils.convert(bo, CvVendorTableField.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        return baseMapper.deleteByIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<CvVendorTableField> buildQueryWrapper(CvVendorTableFieldBo bo) {
        LambdaQueryWrapper<CvVendorTableField> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StringUtils.isNotBlank(bo.getTableGroup()), CvVendorTableField::getTableGroup, bo.getTableGroup());
        lqw.eq(StringUtils.isNotBlank(bo.getTableCode()), CvVendorTableField::getTableCode, bo.getTableCode());
        lqw.like(StringUtils.isNotBlank(bo.getFieldKey()), CvVendorTableField::getFieldKey, bo.getFieldKey());
        lqw.like(StringUtils.isNotBlank(bo.getFieldLabel()), CvVendorTableField::getFieldLabel, bo.getFieldLabel());
        lqw.eq(StringUtils.isNotBlank(bo.getFieldType()), CvVendorTableField::getFieldType, bo.getFieldType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), CvVendorTableField::getStatus, bo.getStatus());
        lqw.orderByAsc(CvVendorTableField::getTableGroup);
        lqw.orderByAsc(CvVendorTableField::getTableCode);
        lqw.orderByAsc(CvVendorTableField::getSortOrder);
        lqw.orderByAsc(CvVendorTableField::getId);
        return lqw;
    }

    private void normalizeAndValidate(CvVendorTableFieldBo bo) {
        bo.setTableGroup(trimLower(bo.getTableGroup()));
        bo.setTableCode(StringUtils.trim(bo.getTableCode()));
        bo.setFieldKey(StringUtils.trim(bo.getFieldKey()));
        bo.setFieldLabel(StringUtils.trim(bo.getFieldLabel()));
        bo.setFieldType(trimLower(bo.getFieldType()));
        if (!ALLOWED_TABLE_GROUPS.contains(bo.getTableGroup())) {
            throw new ServiceException("表分组只能是因子表或维表");
        }
        if (bo.getSortOrder() == null) {
            bo.setSortOrder(0);
        }
        if (bo.getRequiredFlag() == null) {
            bo.setRequiredFlag(Boolean.FALSE);
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus("0");
        }
    }

    private void ensureUnique(CvVendorTableFieldBo bo) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<CvVendorTableField>()
            .eq(CvVendorTableField::getTableGroup, bo.getTableGroup())
            .eq(CvVendorTableField::getTableCode, bo.getTableCode())
            .eq(CvVendorTableField::getFieldKey, bo.getFieldKey())
            .ne(bo.getId() != null, CvVendorTableField::getId, bo.getId()));
        if (count != null && count > 0) {
            throw new ServiceException("同一表内字段编码不能重复");
        }
    }

    private String trimLower(String value) {
        return StringUtils.isBlank(value) ? value : value.trim().toLowerCase();
    }
}
