package org.dromara.carbon.vendor.dimension.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.dimension.domain.CvAdminDivision;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactor;
import org.dromara.carbon.vendor.dimension.domain.CvEmissionSourceCategory;
import org.dromara.carbon.vendor.dimension.mapper.CvAdminDivisionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvBaseYearMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvEmissionSourceCategoryMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.dimension.service.ICvDimensionDataService;
import org.dromara.carbon.vendor.shared.VendorManagedTableCatalog;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 统一维度数据 Service 实现
 * <p>按 dimensionCode 路由到对应的具体 Mapper 完成 CRUD。</p>
 *
 * @author carbon
 */
@RequiredArgsConstructor
@Service
public class CvDimensionDataServiceImpl implements ICvDimensionDataService {

    private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,62}$");
    private static final Set<String> READONLY_COLUMNS = Set.of("id", "create_dept", "create_by", "create_time", "update_by", "update_time");
    private static final DefaultIdentifierGenerator IDENTIFIER_GENERATOR = DefaultIdentifierGenerator.getInstance();

    private final CvAdminDivisionMapper adminDivisionMapper;
    private final CvEmissionSourceCategoryMapper emissionSourceCategoryMapper;
    private final CvBaseYearMapper baseYearMapper;
    private final CvElectricityMapper electricityMapper;
    private final CvElectricityFactorVersionMapper electricityFactorVersionMapper;
    private final CvElectricityFactorScopeMapper electricityFactorScopeMapper;
    private final CvGreenhouseGasMapper greenhouseGasMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public TableDataInfo<?> queryPageList(String dimensionCode, PageQuery pageQuery) {
        ManagedDimension dimension = managedDimension(dimensionCode);
        int pageNum = pageQuery.getPageNum() == null || pageQuery.getPageNum() <= 0 ? PageQuery.DEFAULT_PAGE_NUM : pageQuery.getPageNum();
        int pageSize = pageQuery.getPageSize() == null || pageQuery.getPageSize() <= 0 ? PageQuery.DEFAULT_PAGE_SIZE : pageQuery.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;
        String tableName = dimension.tableName();
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + quoteTable(tableName) + " WHERE " + quoteIdentifier("status") + " = '0'", Long.class);
        List<Map<String, Object>> rows = queryPageRows(tableName, offset, pageSize);
        TableDataInfo<Map<String, Object>> dataInfo = new TableDataInfo<>();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(rows.stream().map(row -> toRecordMap(dimensionCode, row)).toList());
        dataInfo.setTotal(total == null ? 0L : total);
        return dataInfo;
    }

    @Override
    public Map<String, Object> queryById(String dimensionCode, Long id) {
        ManagedDimension dimension = managedDimension(dimensionCode);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + quoteTable(dimension.tableName()) + " WHERE " + quoteIdentifier("id") + " = ?", id);
        return rows.isEmpty() ? null : toRecordMap(dimensionCode, rows.get(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertByBo(String dimensionCode, Map<String, Object> bo) {
        ManagedDimension dimension = managedDimension(dimensionCode);
        applyRecordFields(dimensionCode, bo);
        validateUniqueBusinessKeys(dimensionCode, dimension.tableName(), bo, null);
        Map<String, Object> values = writableValues(dimension.tableName(), bo, false);
        return insertRow(dimension.tableName(), values);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(String dimensionCode, Map<String, Object> bo) {
        ManagedDimension dimension = managedDimension(dimensionCode);
        applyRecordFields(dimensionCode, bo);
        Map<String, Object> values = writableValues(dimension.tableName(), bo, true);
        Object id = bo.get("id");
        if (id == null) {
            throw new ServiceException("id不能为空");
        }
        validateUniqueBusinessKeys(dimensionCode, dimension.tableName(), bo, id);
        return updateRow(dimension.tableName(), values, id);
    }

    @Override
    public int deleteByIds(String dimensionCode, Collection<Long> ids) {
        return switch (dimensionCode) {
            case "admin-division" -> {
                validateAdminDivisionDelete(ids);
                yield adminDivisionMapper.deleteByIds(ids);
            }
            case "emission-source-category" -> {
                validateEmissionSourceCategoryDelete(ids);
                yield emissionSourceCategoryMapper.deleteByIds(ids);
            }
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
        ManagedDimension dimension = managedDimension(dimensionCode);
        return jdbcTemplate.queryForList("SELECT * FROM " + quoteTable(dimension.tableName()) + " WHERE " + quoteIdentifier("status") + " = '0' ORDER BY " + quoteIdentifier("sort_order") + " ASC, " + quoteIdentifier("id") + " ASC")
            .stream()
            .map(row -> toRecordMap(dimensionCode, row))
            .toList();
    }

    // ==================== helpers ====================

    private void validateAdminDivisionDelete(Collection<Long> ids) {
        List<CvAdminDivision> records = adminDivisionMapper.selectList(new QueryWrapper<CvAdminDivision>().in("id", ids));
        if (records.isEmpty()) {
            return;
        }
        List<String> divisionCodes = records.stream().map(CvAdminDivision::getDivisionCode).toList();
        Long childCount = adminDivisionMapper.selectCount(new QueryWrapper<CvAdminDivision>()
            .in("parent_code", divisionCodes)
            .notIn("division_code", divisionCodes));
        if (childCount != null && childCount > 0) {
            throw new ServiceException("行政区划存在下级区划引用，请先删除下级区划");
        }
        Long factorCount = electricityMapper.selectCount(new QueryWrapper<CvElectricityFactor>()
            .in("division_code", divisionCodes));
        if (factorCount != null && factorCount > 0) {
            throw new ServiceException("行政区划已被电力因子引用，不能删除");
        }
    }

    private void validateEmissionSourceCategoryDelete(Collection<Long> ids) {
        List<CvEmissionSourceCategory> records = emissionSourceCategoryMapper.selectList(new QueryWrapper<CvEmissionSourceCategory>().in("id", ids));
        if (records.isEmpty()) {
            return;
        }
        List<String> categoryCodes = records.stream().map(CvEmissionSourceCategory::getCategoryCode).toList();
        Long childCount = emissionSourceCategoryMapper.selectCount(new QueryWrapper<CvEmissionSourceCategory>()
            .in("parent_code", categoryCodes)
            .notIn("category_code", categoryCodes));
        if (childCount != null && childCount > 0) {
            throw new ServiceException("排放源分类存在下级分类引用，请先删除下级分类");
        }
    }

    private Map<String, Object> toRecordMap(String dimensionCode, Map<String, Object> entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entity.entrySet()) {
            map.put(StringUtils.toCamelCase(entry.getKey()), normalizeValue(entry.getValue()));
        }
        map.put("dimensionCode", dimensionCode);
        map.put("recordCode", firstValue(map, codeField(dimensionCode)));
        map.put("recordName", firstValue(map, nameField(dimensionCode), codeField(dimensionCode)));
        return map;
    }

    private void applyRecordFields(String dimensionCode, Map<String, Object> bo) {
        String codeField = codeField(dimensionCode);
        String nameField = nameField(dimensionCode);
        Object recordCode = bo.get("recordCode");
        Object recordName = bo.get("recordName");
        if ("base-year".equals(dimensionCode)) {
            applyBaseYearRecordFields(bo, recordCode, recordName);
            return;
        }
        if (recordCode != null) {
            bo.put(codeField, recordCode);
        }
        if (!codeField.equals(nameField) && recordName != null) {
            bo.put(nameField, recordName);
        }
    }

    private void validateUniqueBusinessKeys(String dimensionCode, String tableName, Map<String, Object> bo, Object currentId) {
        if (!"ef-electricity-version".equals(dimensionCode)) {
            return;
        }
        Object factorVersion = firstNonNull(bo.get("factorVersion"), bo.get("factor_version"), bo.get("recordCode"));
        if (factorVersion == null || StringUtils.isBlank(String.valueOf(factorVersion))) {
            throw new ServiceException("版本号不能为空");
        }
        String sql = "SELECT COUNT(*) FROM " + quoteTable(tableName) + " WHERE " + quoteIdentifier("factor_version") + " = ?";
        List<Object> args = new ArrayList<>();
        args.add(String.valueOf(factorVersion).trim());
        if (currentId != null) {
            sql += " AND " + quoteIdentifier("id") + " <> ?";
            args.add(currentId);
        }
        Long count = jdbcTemplate.queryForObject(sql, Long.class, args.toArray());
        if (count != null && count > 0) {
            throw new ServiceException("版本号已存在");
        }
    }

    private void applyBaseYearRecordFields(Map<String, Object> bo, Object recordCode, Object recordName) {
        Object baseYearKey = firstNonNull(recordCode, bo.get("baseYearKey"));
        Object baseYear = firstNonNull(recordName, bo.get("baseYear"));
        if (baseYearKey != null) {
            bo.put("baseYearKey", baseYearKey);
            bo.put("recordCode", baseYearKey);
        }
        if (baseYear != null) {
            Integer normalizedBaseYear = normalizeBaseYear(baseYear);
            bo.put("baseYear", normalizedBaseYear);
            bo.put("recordName", normalizedBaseYear);
        }
        if (bo.get("isCurrent") == null) {
            bo.put("isCurrent", 1);
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Integer normalizeBaseYear(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ServiceException("基准年份必须为数字");
        }
    }

    private Object firstValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String codeField(String dimensionCode) {
        return switch (dimensionCode) {
            case "admin-division" -> "divisionCode";
            case "ef-electricity-factor" -> "versionProvinceCode";
            case "emission-source-category" -> "categoryCode";
            case "base-year" -> "baseYearKey";
            case "ef-electricity-version" -> "factorVersion";
            case "ef-electricity-scope" -> "scopeKey";
            case "greenhouse-gas" -> "gasCode";
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
    }

    private String nameField(String dimensionCode) {
        return switch (dimensionCode) {
            case "admin-division", "ef-electricity-factor" -> "divisionName";
            case "emission-source-category" -> "categoryName";
            case "base-year" -> "baseYear";
            case "ef-electricity-version" -> "factorVersion";
            case "ef-electricity-scope" -> "scopeName";
            case "greenhouse-gas" -> "gasName";
            default -> throw new ServiceException("不支持的维度编码: " + dimensionCode);
        };
    }

    private ManagedDimension managedDimension(String dimensionCode) {
        if (!VendorManagedTableCatalog.isManagedTable("dimension", dimensionCode)
            && !VendorManagedTableCatalog.isManagedTable("factor", dimensionCode)) {
            throw new ServiceException("不支持的维度编码: " + dimensionCode);
        }
        String tableName = VendorManagedTableCatalog.physicalTableName("dimension", dimensionCode);
        if (StringUtils.isBlank(tableName)) {
            tableName = VendorManagedTableCatalog.physicalTableName("factor", dimensionCode);
        }
        if (StringUtils.isBlank(tableName)) {
            throw new ServiceException("缺少维度表映射: " + dimensionCode);
        }
        return new ManagedDimension(dimensionCode, tableName);
    }

    private List<Map<String, Object>> queryPageRows(String tableName, long offset, int pageSize) {
        String orderSql = " ORDER BY " + quoteIdentifier("sort_order") + " ASC, " + quoteIdentifier("id") + " ASC";
        return jdbcTemplate.queryForList(
            "SELECT * FROM " + quoteTable(tableName) + " WHERE " + quoteIdentifier("status") + " = '0'" + orderSql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
            offset,
            pageSize
        );
    }

    private List<String> physicalColumns(String tableName) {
        return jdbcTemplate.queryForList("""
            SELECT column_name
            FROM information_schema.columns
            WHERE table_schema = 'dbo' AND table_name = ?
              AND COLUMNPROPERTY(OBJECT_ID(table_schema + '.' + table_name), column_name, 'IsComputed') = 0
            ORDER BY ordinal_position
            """, String.class, tableName);
    }

    private Map<String, Object> writableValues(String tableName, Map<String, Object> bo, boolean update) {
        List<String> columns = physicalColumns(tableName);
        Map<String, Object> values = new LinkedHashMap<>();
        for (String column : columns) {
            if (READONLY_COLUMNS.contains(column) || !COLUMN_NAME_PATTERN.matcher(column).matches()) {
                continue;
            }
            String camelKey = StringUtils.toCamelCase(column);
            if (bo.containsKey(camelKey)) {
                values.put(column, bo.get(camelKey));
            } else if (bo.containsKey(column)) {
                values.put(column, bo.get(column));
            }
        }
        values.put("update_time", new Timestamp(System.currentTimeMillis()));
        if (!update) {
            values.put("id", IDENTIFIER_GENERATOR.nextId(null));
            values.putIfAbsent("status", "0");
            values.put("create_time", new Timestamp(System.currentTimeMillis()));
        }
        return values;
    }

    private int insertRow(String tableName, Map<String, Object> values) {
        if (values.isEmpty()) {
            throw new ServiceException("没有可写入字段");
        }
        List<String> columns = new ArrayList<>(values.keySet());
        String columnSql = columns.stream().map(this::quoteIdentifier).reduce((left, right) -> left + ", " + right).orElse("");
        String placeholderSql = String.join(", ", columns.stream().map(column -> "?").toList());
        return jdbcTemplate.update("INSERT INTO " + quoteTable(tableName) + " (" + columnSql + ") VALUES (" + placeholderSql + ")",
            columns.stream().map(values::get).toArray());
    }

    private int updateRow(String tableName, Map<String, Object> values, Object id) {
        if (values.isEmpty()) {
            throw new ServiceException("没有可更新字段");
        }
        List<String> columns = new ArrayList<>(values.keySet());
        String setSql = columns.stream().map(column -> quoteIdentifier(column) + " = ?").reduce((left, right) -> left + ", " + right).orElse("");
        List<Object> args = new ArrayList<>(columns.stream().map(values::get).toList());
        args.add(id);
        return jdbcTemplate.update("UPDATE " + quoteTable(tableName) + " SET " + setSql + " WHERE " + quoteIdentifier("id") + " = ?", args.toArray());
    }

    private Object normalizeValue(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDate || value instanceof LocalDateTime) {
            return value;
        }
        return value;
    }

    private String quoteTable(String tableName) {
        return "dbo." + quoteIdentifier(tableName);
    }

    private String quoteIdentifier(String identifier) {
        return "[" + identifier.replace("]", "]]") + "]";
    }

    private record ManagedDimension(String dimensionCode, String tableName) {
    }
}
