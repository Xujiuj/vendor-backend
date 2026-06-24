package org.dromara.carbon.vendor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.dimension.CvAdminDivisionBo;
import org.dromara.carbon.vendor.domain.bo.dimension.CvBaseYearBo;
import org.dromara.carbon.vendor.domain.bo.dimension.CvElectricityFactorBo;
import org.dromara.carbon.vendor.domain.bo.dimension.CvElectricityFactorScopeBo;
import org.dromara.carbon.vendor.domain.bo.dimension.CvElectricityFactorVersionBo;
import org.dromara.carbon.vendor.domain.bo.dimension.CvEmissionSourceCategoryBo;
import org.dromara.carbon.vendor.domain.bo.dimension.CvGreenhouseGasBo;
import org.dromara.carbon.vendor.domain.dimension.CvAdminDivision;
import org.dromara.carbon.vendor.domain.dimension.CvBaseYear;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactor;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactorScope;
import org.dromara.carbon.vendor.domain.dimension.CvElectricityFactorVersion;
import org.dromara.carbon.vendor.domain.dimension.CvEmissionSourceCategory;
import org.dromara.carbon.vendor.domain.dimension.CvGreenhouseGas;
import org.dromara.carbon.vendor.mapper.dimension.CvAdminDivisionMapper;
import org.dromara.carbon.vendor.mapper.dimension.CvBaseYearMapper;
import org.dromara.carbon.vendor.mapper.dimension.CvElectricityMapper;
import org.dromara.carbon.vendor.mapper.dimension.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.mapper.dimension.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.mapper.dimension.CvEmissionSourceCategoryMapper;
import org.dromara.carbon.vendor.mapper.dimension.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.service.ICvDimensionDataService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一维度数据 Service 实现
 * <p>按 dimensionCode 路由到对应的具体 Mapper 完成 CRUD。</p>
 *
 * @author carbon
 */
@RequiredArgsConstructor
@Service
public class CvDimensionDataServiceImpl implements ICvDimensionDataService {

    private final CvAdminDivisionMapper adminDivisionMapper;
    private final CvEmissionSourceCategoryMapper emissionSourceCategoryMapper;
    private final CvBaseYearMapper baseYearMapper;
    private final CvElectricityMapper electricityMapper;
    private final CvElectricityFactorVersionMapper electricityFactorVersionMapper;
    private final CvElectricityFactorScopeMapper electricityFactorScopeMapper;
    private final CvGreenhouseGasMapper greenhouseGasMapper;

    @Override
    public TableDataInfo<?> queryPageList(String dimensionCode, PageQuery pageQuery) {
        return switch (dimensionCode) {
            case "admin-division" -> TableDataInfo.build(adminDivisionMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvAdminDivision.class)));
            case "emission-source-category" -> TableDataInfo.build(emissionSourceCategoryMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvEmissionSourceCategory.class)));
            case "base-year" -> TableDataInfo.build(baseYearMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvBaseYear.class)));
            case "ef-electricity-factor" -> TableDataInfo.build(electricityMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvElectricityFactor.class)));
            case "ef-electricity-version" -> TableDataInfo.build(electricityFactorVersionMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvElectricityFactorVersion.class)));
            case "ef-electricity-scope" -> TableDataInfo.build(electricityFactorScopeMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvElectricityFactorScope.class)));
            case "greenhouse-gas" -> TableDataInfo.build(greenhouseGasMapper.selectPage(
                pageQuery.build(), defaultWrapper(CvGreenhouseGas.class)));
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
    }

    @Override
    public Map<String, Object> queryById(String dimensionCode, Long id) {
        Object entity = switch (dimensionCode) {
            case "admin-division" -> adminDivisionMapper.selectById(id);
            case "emission-source-category" -> emissionSourceCategoryMapper.selectById(id);
            case "base-year" -> baseYearMapper.selectById(id);
            case "ef-electricity-factor" -> electricityMapper.selectById(id);
            case "ef-electricity-version" -> electricityFactorVersionMapper.selectById(id);
            case "ef-electricity-scope" -> electricityFactorScopeMapper.selectById(id);
            case "greenhouse-gas" -> greenhouseGasMapper.selectById(id);
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
        return entity == null ? null : toMap(entity);
    }

    @Override
    public int insertByBo(String dimensionCode, Map<String, Object> bo) {
        return switch (dimensionCode) {
            case "admin-division" -> {
                CvAdminDivision entity = MapstructUtils.convert(bo, CvAdminDivision.class);
                yield adminDivisionMapper.insert(entity);
            }
            case "emission-source-category" -> {
                CvEmissionSourceCategory entity = MapstructUtils.convert(bo, CvEmissionSourceCategory.class);
                yield emissionSourceCategoryMapper.insert(entity);
            }
            case "base-year" -> {
                CvBaseYear entity = MapstructUtils.convert(bo, CvBaseYear.class);
                yield baseYearMapper.insert(entity);
            }
            case "ef-electricity-factor" -> {
                CvElectricityFactor entity = MapstructUtils.convert(bo, CvElectricityFactor.class);
                yield electricityMapper.insert(entity);
            }
            case "ef-electricity-version" -> {
                CvElectricityFactorVersion entity = MapstructUtils.convert(bo, CvElectricityFactorVersion.class);
                yield electricityFactorVersionMapper.insert(entity);
            }
            case "ef-electricity-scope" -> {
                CvElectricityFactorScope entity = MapstructUtils.convert(bo, CvElectricityFactorScope.class);
                yield electricityFactorScopeMapper.insert(entity);
            }
            case "greenhouse-gas" -> {
                CvGreenhouseGas entity = MapstructUtils.convert(bo, CvGreenhouseGas.class);
                yield greenhouseGasMapper.insert(entity);
            }
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
    }

    @Override
    public int updateByBo(String dimensionCode, Map<String, Object> bo) {
        return switch (dimensionCode) {
            case "admin-division" -> {
                CvAdminDivision entity = MapstructUtils.convert(bo, CvAdminDivision.class);
                yield adminDivisionMapper.updateById(entity);
            }
            case "emission-source-category" -> {
                CvEmissionSourceCategory entity = MapstructUtils.convert(bo, CvEmissionSourceCategory.class);
                yield emissionSourceCategoryMapper.updateById(entity);
            }
            case "base-year" -> {
                CvBaseYear entity = MapstructUtils.convert(bo, CvBaseYear.class);
                yield baseYearMapper.updateById(entity);
            }
            case "ef-electricity-factor" -> {
                CvElectricityFactor entity = MapstructUtils.convert(bo, CvElectricityFactor.class);
                yield electricityMapper.updateById(entity);
            }
            case "ef-electricity-version" -> {
                CvElectricityFactorVersion entity = MapstructUtils.convert(bo, CvElectricityFactorVersion.class);
                yield electricityFactorVersionMapper.updateById(entity);
            }
            case "ef-electricity-scope" -> {
                CvElectricityFactorScope entity = MapstructUtils.convert(bo, CvElectricityFactorScope.class);
                yield electricityFactorScopeMapper.updateById(entity);
            }
            case "greenhouse-gas" -> {
                CvGreenhouseGas entity = MapstructUtils.convert(bo, CvGreenhouseGas.class);
                yield greenhouseGasMapper.updateById(entity);
            }
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
    }

    @Override
    public int deleteByIds(String dimensionCode, Collection<Long> ids) {
        return switch (dimensionCode) {
            case "admin-division" -> adminDivisionMapper.deleteByIds(ids);
            case "emission-source-category" -> emissionSourceCategoryMapper.deleteByIds(ids);
            case "base-year" -> baseYearMapper.deleteByIds(ids);
            case "ef-electricity-factor" -> electricityMapper.deleteByIds(ids);
            case "ef-electricity-version" -> electricityFactorVersionMapper.deleteByIds(ids);
            case "ef-electricity-scope" -> electricityFactorScopeMapper.deleteByIds(ids);
            case "greenhouse-gas" -> greenhouseGasMapper.deleteByIds(ids);
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
    }

    @Override
    public List<Map<String, Object>> queryList(String dimensionCode) {
        List<?> list = switch (dimensionCode) {
            case "admin-division" -> adminDivisionMapper.selectList(defaultWrapper(CvAdminDivision.class));
            case "emission-source-category" -> emissionSourceCategoryMapper.selectList(defaultWrapper(CvEmissionSourceCategory.class));
            case "base-year" -> baseYearMapper.selectList(defaultWrapper(CvBaseYear.class));
            case "ef-electricity-factor" -> electricityMapper.selectList(defaultWrapper(CvElectricityFactor.class));
            case "ef-electricity-version" -> electricityFactorVersionMapper.selectList(defaultWrapper(CvElectricityFactorVersion.class));
            case "ef-electricity-scope" -> electricityFactorScopeMapper.selectList(defaultWrapper(CvElectricityFactorScope.class));
            case "greenhouse-gas" -> greenhouseGasMapper.selectList(defaultWrapper(CvGreenhouseGas.class));
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
        return list.stream().map(this::toMap).toList();
    }

    // ==================== helpers ====================

    private <T> QueryWrapper<T> defaultWrapper(Class<T> entityClass) {
        return new QueryWrapper<T>()
            .eq("status", "0")
            .orderByAsc("sort_order")
            .orderByAsc("id");
    }

    private Map<String, Object> toMap(Object entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (var field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(entity));
            }
            // Include inherited fields
            for (var field : entity.getClass().getSuperclass().getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(entity));
            }
        } catch (IllegalAccessException e) {
            throw new ServiceException("实体转换失败");
        }
        return map;
    }
}
