package org.dromara.carbon.vendor.tablefield.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.shared.VendorManagedTableCatalog;
import org.dromara.carbon.vendor.tablefield.domain.CvVendorTableField;
import org.dromara.carbon.vendor.tablefield.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.tablefield.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.tablefield.service.ICvVendorTableFieldService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CvVendorTableFieldServiceImpl implements ICvVendorTableFieldService {

    private final CvVendorTableFieldMapper baseMapper;

    @Override
    public TableDataInfo<CvVendorTableField> queryPageList(CvVendorTableFieldBo bo, PageQuery pageQuery) {
        IPage<CvVendorTableField> result = baseMapper.selectPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<CvVendorTableField> queryList(CvVendorTableFieldBo bo) {
        validateManagedTable(bo);
        return baseMapper.selectList(buildQueryWrapper(bo));
    }

    @Override
    public CvVendorTableField queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean insertByBo(CvVendorTableFieldBo bo) {
        validateManagedTable(bo);
        CvVendorTableField entity = copyToEntity(bo);
        entity.setId(null);
        entity.setFieldType(StringUtils.isBlank(entity.getFieldType()) ? "text" : entity.getFieldType());
        entity.setRequiredFlag(Boolean.TRUE.equals(entity.getRequiredFlag()));
        entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? "0" : entity.getStatus());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(CvVendorTableFieldBo bo) {
        validateManagedTable(bo);
        CvVendorTableField entity = copyToEntity(bo);
        entity.setUpdateTime(new Date());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteByIds(Collection<Long> ids) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    private LambdaQueryWrapper<CvVendorTableField> buildQueryWrapper(CvVendorTableFieldBo bo) {
        LambdaQueryWrapper<CvVendorTableField> wrapper = Wrappers.lambdaQuery();
        if (bo != null) {
            wrapper.eq(StringUtils.isNotBlank(bo.getTableGroup()), CvVendorTableField::getTableGroup, bo.getTableGroup())
                .eq(StringUtils.isNotBlank(bo.getTableCode()), CvVendorTableField::getTableCode, bo.getTableCode())
                .like(StringUtils.isNotBlank(bo.getFieldKey()), CvVendorTableField::getFieldKey, bo.getFieldKey())
                .like(StringUtils.isNotBlank(bo.getFieldLabel()), CvVendorTableField::getFieldLabel, bo.getFieldLabel())
                .eq(StringUtils.isNotBlank(bo.getFieldType()), CvVendorTableField::getFieldType, bo.getFieldType())
                .eq(StringUtils.isNotBlank(bo.getStatus()), CvVendorTableField::getStatus, bo.getStatus());
        }
        return wrapper.orderByAsc(CvVendorTableField::getTableGroup)
            .orderByAsc(CvVendorTableField::getTableCode)
            .orderByAsc(CvVendorTableField::getSortOrder)
            .orderByAsc(CvVendorTableField::getId);
    }

    private void validateManagedTable(CvVendorTableFieldBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getTableGroup()) || StringUtils.isBlank(bo.getTableCode())) {
            throw new ServiceException("tableGroup and tableCode cannot be blank");
        }
        String tableGroup = bo.getTableGroup().trim();
        String tableCode = bo.getTableCode().trim();
        if (!VendorManagedTableCatalog.isManagedTable(tableGroup, tableCode)) {
            throw new ServiceException("unsupported vendor table field target: " + tableGroup + "/" + tableCode);
        }
    }

    private CvVendorTableField copyToEntity(CvVendorTableFieldBo bo) {
        CvVendorTableField entity = new CvVendorTableField();
        entity.setId(bo.getId());
        entity.setTableGroup(bo.getTableGroup().trim());
        entity.setTableCode(bo.getTableCode().trim());
        entity.setFieldKey(bo.getFieldKey());
        entity.setFieldLabel(bo.getFieldLabel());
        entity.setFieldType(bo.getFieldType());
        entity.setFieldPrecision(bo.getFieldPrecision());
        entity.setFieldWidth(bo.getFieldWidth());
        entity.setRequiredFlag(bo.getRequiredFlag());
        entity.setSortOrder(bo.getSortOrder());
        entity.setStatus(bo.getStatus());
        entity.setRemark(bo.getRemark());
        return entity;
    }
}
