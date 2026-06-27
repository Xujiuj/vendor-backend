package org.dromara.carbon.vendor.tablefield.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.shared.VendorManagedTableCatalog;
import org.dromara.carbon.vendor.tablefield.domain.CvVendorTableField;
import org.dromara.carbon.vendor.tablefield.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.tablefield.domain.vo.CvVendorTableFieldVo;
import org.dromara.carbon.vendor.tablefield.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.tablefield.service.ICvVendorTableFieldService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class CvVendorTableFieldServiceImpl implements ICvVendorTableFieldService {

    private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,62}$");
    private static final Set<String> IMMUTABLE_COLUMNS = Set.of("id", "create_dept", "create_by", "create_time", "update_by", "update_time");
    private static final Set<String> FIELD_TYPES = Set.of("text", "number", "date", "datetime", "select", "boolean");

    private final CvVendorTableFieldMapper baseMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public TableDataInfo<CvVendorTableFieldVo> queryPageList(CvVendorTableFieldBo bo, PageQuery pageQuery) {
        List<CvVendorTableFieldVo> allRows = queryList(bo);
        int pageNum = pageQuery.getPageNum() == null || pageQuery.getPageNum() <= 0 ? PageQuery.DEFAULT_PAGE_NUM : pageQuery.getPageNum();
        int pageSize = pageQuery.getPageSize() == null || pageQuery.getPageSize() <= 0 ? PageQuery.DEFAULT_PAGE_SIZE : pageQuery.getPageSize();
        int fromIndex = Math.min((pageNum - 1) * pageSize, allRows.size());
        int toIndex = Math.min(fromIndex + pageSize, allRows.size());
        TableDataInfo<CvVendorTableFieldVo> dataInfo = new TableDataInfo<>();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(allRows.subList(fromIndex, toIndex));
        dataInfo.setTotal(allRows.size());
        return dataInfo;
    }

    @Override
    public List<CvVendorTableFieldVo> queryList(CvVendorTableFieldBo bo) {
        ManagedTable table = validateManagedTable(bo);
        Map<String, CvVendorTableField> configMap = new LinkedHashMap<>();
        baseMapper.selectList(buildQueryWrapper(bo)).forEach(config -> configMap.put(config.getFieldKey(), config));
        return queryPhysicalColumns(table.physicalTableName()).stream()
            .filter(column -> matchesQuery(column, bo))
            .map(column -> toVo(table, column, configMap.get(column.columnName())))
            .toList();
    }

    @Override
    public CvVendorTableFieldVo queryById(Long id) {
        CvVendorTableField config = baseMapper.selectById(id);
        if (config == null) {
            return null;
        }
        ManagedTable table = validateManagedTable(config.getTableGroup(), config.getTableCode());
        ColumnMeta column = queryPhysicalColumns(table.physicalTableName()).stream()
            .filter(item -> item.columnName().equals(config.getFieldKey()))
            .findFirst()
            .orElse(null);
        if (column == null) {
            CvVendorTableFieldVo vo = BeanUtil.copyProperties(config, CvVendorTableFieldVo.class);
            vo.setPhysicalTableName(table.physicalTableName());
            vo.setColumnName(config.getFieldKey());
            return vo;
        }
        return toVo(table, column, config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertByBo(CvVendorTableFieldBo bo) {
        ManagedTable table = validateManagedTable(bo);
        normalizeAndValidateField(bo);
        if (!physicalColumnExists(table.physicalTableName(), bo.getFieldKey())) {
            jdbcTemplate.execute(buildAddColumnSql(table.physicalTableName(), bo));
        }
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
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByBo(CvVendorTableFieldBo bo) {
        ManagedTable table = validateManagedTable(bo);
        normalizeAndValidateField(bo);
        if (!physicalColumnExists(table.physicalTableName(), bo.getFieldKey())) {
            throw new ServiceException("数据库字段不存在: " + bo.getFieldKey());
        }
        updateColumnComment(table.physicalTableName(), bo);
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

    private ManagedTable validateManagedTable(CvVendorTableFieldBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getTableGroup()) || StringUtils.isBlank(bo.getTableCode())) {
            throw new ServiceException("tableGroup and tableCode cannot be blank");
        }
        return validateManagedTable(bo.getTableGroup(), bo.getTableCode());
    }

    private ManagedTable validateManagedTable(String tableGroupValue, String tableCodeValue) {
        String tableGroup = tableGroupValue.trim();
        String tableCode = tableCodeValue.trim();
        if (!VendorManagedTableCatalog.isManagedTable(tableGroup, tableCode)) {
            throw new ServiceException("unsupported vendor table field target: " + tableGroup + "/" + tableCode);
        }
        String physicalTableName = VendorManagedTableCatalog.physicalTableName(tableGroup, tableCode);
        if (StringUtils.isBlank(physicalTableName)) {
            throw new ServiceException("missing physical table mapping: " + tableGroup + "/" + tableCode);
        }
        return new ManagedTable(tableGroup, tableCode, physicalTableName);
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
        entity.setFieldOptions(bo.getFieldOptions());
        entity.setRequiredFlag(bo.getRequiredFlag());
        entity.setSortOrder(bo.getSortOrder());
        entity.setStatus(bo.getStatus());
        entity.setRemark(bo.getRemark());
        return entity;
    }

    private void normalizeAndValidateField(CvVendorTableFieldBo bo) {
        bo.setTableGroup(bo.getTableGroup().trim());
        bo.setTableCode(bo.getTableCode().trim());
        bo.setFieldKey(bo.getFieldKey().trim().toLowerCase(Locale.ROOT));
        bo.setFieldType(StringUtils.isBlank(bo.getFieldType()) ? "text" : bo.getFieldType().trim());
        if (!COLUMN_NAME_PATTERN.matcher(bo.getFieldKey()).matches()) {
            throw new ServiceException("字段编码必须是数据库字段名，仅支持小写字母、数字、下划线，且以字母开头");
        }
        if (IMMUTABLE_COLUMNS.contains(bo.getFieldKey())) {
            throw new ServiceException("系统字段不允许自定义: " + bo.getFieldKey());
        }
        if (!FIELD_TYPES.contains(bo.getFieldType())) {
            throw new ServiceException("不支持的字段类型: " + bo.getFieldType());
        }
        if ("select".equals(bo.getFieldType()) && StringUtils.isBlank(bo.getFieldOptions())) {
            throw new ServiceException("选项字段必须填写选项");
        }
        if (bo.getFieldWidth() != null && (bo.getFieldWidth() < 1 || bo.getFieldWidth() > 1000)) {
            throw new ServiceException("字段长度范围必须为 1-1000");
        }
        if (bo.getFieldPrecision() != null && (bo.getFieldPrecision() < 0 || bo.getFieldPrecision() > 10)) {
            throw new ServiceException("小数位范围必须为 0-10");
        }
    }

    private List<ColumnMeta> queryPhysicalColumns(String tableName) {
        String sql = """
            SELECT column_name, column_type, data_type, column_comment, is_nullable, extra, ordinal_position
            FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = ?
            ORDER BY ordinal_position
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ColumnMeta(
            rs.getString("column_name"),
            rs.getString("column_type"),
            rs.getString("data_type"),
            rs.getString("column_comment"),
            "YES".equalsIgnoreCase(rs.getString("is_nullable")),
            containsIgnoreCase(rs.getString("extra"), "generated"),
            rs.getInt("ordinal_position")
        ), tableName);
    }

    private boolean physicalColumnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
            """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private boolean matchesQuery(ColumnMeta column, CvVendorTableFieldBo bo) {
        if (bo == null) {
            return true;
        }
        if (StringUtils.isNotBlank(bo.getFieldKey()) && !containsIgnoreCase(column.columnName(), bo.getFieldKey())) {
            return false;
        }
        return StringUtils.isBlank(bo.getFieldLabel()) || containsIgnoreCase(column.columnComment(), bo.getFieldLabel());
    }

    private CvVendorTableFieldVo toVo(ManagedTable table, ColumnMeta column, CvVendorTableField config) {
        CvVendorTableFieldVo vo = config == null ? new CvVendorTableFieldVo() : BeanUtil.copyProperties(config, CvVendorTableFieldVo.class);
        vo.setTableGroup(table.tableGroup());
        vo.setTableCode(table.tableCode());
        vo.setPhysicalTableName(table.physicalTableName());
        vo.setColumnName(column.columnName());
        vo.setFieldKey(column.columnName());
        vo.setFieldLabel(StringUtils.isBlank(vo.getFieldLabel()) ? defaultLabel(column) : vo.getFieldLabel());
        vo.setFieldType(StringUtils.isBlank(vo.getFieldType()) ? inferFieldType(column.dataType()) : vo.getFieldType());
        vo.setRequiredFlag(config == null ? !column.nullable() : Boolean.TRUE.equals(config.getRequiredFlag()));
        vo.setSortOrder(config == null ? column.ordinalPosition() : config.getSortOrder());
        vo.setStatus(StringUtils.isBlank(vo.getStatus()) ? "0" : vo.getStatus());
        vo.setColumnType(column.columnType());
        vo.setDataType(column.dataType());
        vo.setColumnComment(column.columnComment());
        vo.setNullable(column.nullable());
        vo.setGeneratedColumn(column.generatedColumn());
        return vo;
    }

    private String defaultLabel(ColumnMeta column) {
        return StringUtils.isBlank(column.columnComment()) ? column.columnName() : column.columnComment();
    }

    private String inferFieldType(String dataType) {
        String type = dataType == null ? "" : dataType.toLowerCase(Locale.ROOT);
        if (Set.of("int", "bigint", "decimal", "double", "float", "tinyint").contains(type)) {
            return "number";
        }
        if ("date".equals(type)) {
            return "date";
        }
        if (type.contains("time")) {
            return "datetime";
        }
        return "text";
    }

    private String buildAddColumnSql(String tableName, CvVendorTableFieldBo bo) {
        String definition = columnDefinition(bo);
        return "ALTER TABLE `" + tableName + "` ADD COLUMN `" + bo.getFieldKey() + "` " + definition;
    }

    private void updateColumnComment(String tableName, CvVendorTableFieldBo bo) {
        ColumnMeta column = queryPhysicalColumns(tableName).stream()
            .filter(item -> item.columnName().equals(bo.getFieldKey()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("数据库字段不存在: " + bo.getFieldKey()));
        if (column.generatedColumn() || IMMUTABLE_COLUMNS.contains(bo.getFieldKey())) {
            return;
        }
        String comment = escapeSqlComment(defaultIfBlank(bo.getFieldLabel(), column.columnComment()));
        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + bo.getFieldKey() + "` "
            + column.columnType() + (column.nullable() ? " NULL" : " NOT NULL") + " COMMENT '" + comment + "'");
    }

    private String columnDefinition(CvVendorTableFieldBo bo) {
        String nullClause = " NULL";
        String comment = " COMMENT '" + escapeSqlComment(bo.getFieldLabel()) + "'";
        return switch (bo.getFieldType()) {
            case "number" -> {
                int scale = bo.getFieldPrecision() == null ? 4 : bo.getFieldPrecision();
                yield "DECIMAL(28, " + scale + ")" + nullClause + comment;
            }
            case "date" -> "DATE" + nullClause + comment;
            case "datetime" -> "DATETIME" + nullClause + comment;
            case "boolean" -> "TINYINT(1)" + nullClause + comment;
            case "select", "text" -> "VARCHAR(" + normalizeTextWidth(bo.getFieldWidth()) + ")" + nullClause + comment;
            default -> throw new ServiceException("不支持的字段类型: " + bo.getFieldType());
        };
    }

    private int normalizeTextWidth(Integer width) {
        if (width != null) {
            return Math.max(1, Math.min(width, 1000));
        }
        return 255;
    }

    private String escapeSqlComment(String value) {
        return defaultIfBlank(value, "").replace("'", "''");
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private boolean containsIgnoreCase(String source, String search) {
        if (source == null || search == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    private record ManagedTable(String tableGroup, String tableCode, String physicalTableName) {
    }

    private record ColumnMeta(String columnName, String columnType, String dataType, String columnComment,
                              boolean nullable, boolean generatedColumn, int ordinalPosition) {
    }
}
