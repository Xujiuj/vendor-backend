package org.dromara.carbon.vendor.factor.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dromara.carbon.vendor.factor.domain.CvSourceAFactorImportResult;
import org.dromara.carbon.vendor.factor.service.ICvSourceAFactorImportService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Source(A) factor workbook import service implementation.
 * Imports the 6 sheets from '2 排放因子表.xlsx' into vendor cv_* tables.
 *
 * @author Claude
 */
@RequiredArgsConstructor
@Service
public class CvSourceAFactorImportServiceImpl implements ICvSourceAFactorImportService {

    private static final String MARK = "source(A)";
    private static final String SOURCE_A_VERSION_CODE = "source(A)";

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CvSourceAFactorImportResult importWorkbook(Path workbookPath) {
        if (workbookPath == null || !Files.isRegularFile(workbookPath)) {
            throw new ServiceException("Workbook path does not exist: " + workbookPath);
        }
        String fileName = workbookPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".xlsx")) {
            throw new ServiceException("Only .xlsx files are supported: " + fileName);
        }

        FactorData data = new FactorData();
        try (InputStream is = Files.newInputStream(workbookPath);
             Workbook workbook = WorkbookFactory.create(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                if (sheetName == null || sheetName.contains("说明")) {
                    continue;
                }
                List<Map<String, Object>> rows = readSheet(sheet);
                if (rows.isEmpty()) {
                    continue;
                }
                routeRows(sheetName, rows, data);
            }
        } catch (IOException e) {
            throw new ServiceException("Failed to read workbook: " + e.getMessage());
        }

        return writeAll(data);
    }

    private List<Map<String, Object>> readSheet(Sheet sheet) {
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow == null) {
            return List.of();
        }
        List<String> headers = new ArrayList<>();
        short lastCell = headerRow.getLastCellNum();
        for (int i = 0; i < lastCell; i++) {
            headers.add(text(cellValue(headerRow.getCell(i))));
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int rowNum = sheet.getFirstRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            Map<String, Object> values = new LinkedHashMap<>();
            boolean nonEmpty = false;
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                if (StringUtils.isBlank(header)) {
                    continue;
                }
                Object value = normalize(cellValue(row.getCell(i)));
                if (value != null) {
                    nonEmpty = true;
                }
                values.put(header, value);
            }
            if (nonEmpty) {
                rows.add(values);
            }
        }
        return rows;
    }

    private Object cellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        return switch (type) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toLocalDate()
                : BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros();
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> null;
        };
    }

    private Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            String trimmed = text.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
        return value;
    }

    private void routeRows(String sheetName, List<Map<String, Object>> rows, FactorData data) {
        if (sheetName.equals("201EF排放因子维度表")) {
            data.ef201Rows.addAll(rows);
        } else if (sheetName.equals("202EF电力因子维度表")) {
            data.ef202Rows.addAll(rows);
        } else if (sheetName.equals("203EF电力因子版本对应")) {
            data.ef203Rows.addAll(rows);
        } else if (sheetName.equals("204EF燃料因子计算")) {
            data.ef204Rows.addAll(rows);
        } else if (sheetName.equals("205EF电力因子口径维度")) {
            data.ef205Rows.addAll(rows);
        } else if (sheetName.equals("206温室气体维度")) {
            data.gas206Rows.addAll(rows);
        }
    }

    private CvSourceAFactorImportResult writeAll(FactorData data) {
        CvSourceAFactorImportResult result = new CvSourceAFactorImportResult();

        clearSourceAData();

        ensureSourceAVersionId();

        result.setImported201EfCount(0);
        result.setImported202EfCount(insert202Ef(data.ef202Rows));
        result.setImported203EfCount(insert203Ef(data.ef203Rows));
        result.setImported204EfCount(0);
        result.setImported205EfCount(insert205Ef(data.ef205Rows));
        result.setImported206GasCount(insert206Gas(data.gas206Rows));

        int total = result.getImported201EfCount() + result.getImported202EfCount()
            + result.getImported203EfCount() + result.getImported204EfCount()
            + result.getImported205EfCount() + result.getImported206GasCount();
        result.setImported(true);
        result.setMessage("Imported " + total + " rows from source(A) workbook");
        return result;
    }

    private void clearSourceAData() {
        jdbcTemplate.update("DELETE FROM cv_electricity_factor WHERE remark = ?", MARK);
        jdbcTemplate.update("DELETE FROM cv_electricity_factor_version WHERE remark = ?", MARK);
        jdbcTemplate.update("DELETE FROM cv_electricity_factor_scope WHERE remark = ?", MARK);
        jdbcTemplate.update("DELETE FROM cv_greenhouse_gas WHERE remark = ?", MARK);
    }

    /**
     * Find or create the source(A) version in cv_factor_version and return its id.
     */
    private Long ensureSourceAVersionId() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM cv_factor_version WHERE version_code = ?", Integer.class, SOURCE_A_VERSION_CODE);
        if (count == null || count == 0) {
            jdbcTemplate.update("""
                INSERT INTO cv_factor_version
                (version_code, version_name, publish_status, frozen_flag, remark, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())
                """, SOURCE_A_VERSION_CODE, "Source(A) Import", "draft", false, MARK);
        }
        return jdbcTemplate.queryForObject(
            "SELECT id FROM cv_factor_version WHERE version_code = ?", Long.class, SOURCE_A_VERSION_CODE);
    }

    private int insert202Ef(List<Map<String, Object>> rows) {
        String sql = """
            INSERT INTO cv_electricity_factor
            (version_province_code, factor_version, division_code, division_name, region_name,
             province_factor, region_factor, national_factor, non_fossil_excluded_factor,
             national_fossil_power_factor, sort_order, status, remark, create_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, '0', ?, SYSDATETIME(), SYSDATETIME())
            """;
        List<Object[]> batchArgs = mapRows(rows, row -> {
            String versionProvinceCode = text(row.get("PK_因子版本省份代码"));
            String version = text(row.get("因子版本"));
            String divisionCode = text(row.get("行政区划代码"));
            if (StringUtils.isBlank(version) || StringUtils.isBlank(divisionCode)) {
                return null;
            }
            return new Object[]{
                StringUtils.isNotBlank(versionProvinceCode) ? versionProvinceCode : version + divisionCode,
                version, divisionCode, row.get("行政区划"), row.get("区域划分"),
                decimal(row.get("省级因子（kgCO2/kWh)")), decimal(row.get("区域因子（kgCO2/kWh)")),
                decimal(row.get("全国因子（kgCO2/kWh）")),
                decimal(row.get("不包括市场化交易的非化石能源电量因子（kgCO2/kWh）")),
                decimal(row.get("全国化石能源电力二氧化碳排放因子（kgCO2/kWh）")), MARK
            };
        });
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return batchArgs.size();
    }

    private int insert203Ef(List<Map<String, Object>> rows) {
        String sql = """
            MERGE INTO cv_electricity_factor_version AS target
            USING (SELECT ? AS factor_version, ? AS effective_year, ? AS remark) AS source
               ON target.factor_version = source.factor_version
            WHEN MATCHED THEN
                UPDATE SET effective_year = CASE
                        WHEN target.effective_year <= source.effective_year THEN target.effective_year
                        ELSE source.effective_year
                    END,
                    remark = source.remark,
                    update_time = SYSDATETIME()
            WHEN NOT MATCHED THEN
                INSERT (factor_version, effective_year, sort_order, status, remark, create_time, update_time)
                VALUES (source.factor_version, source.effective_year, 0, '0', source.remark, SYSDATETIME(), SYSDATETIME());
            """;
        Map<String, Integer> versionYears = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Integer year = integer(row.get("年份"));
            String version = text(row.get("对应因子版本"));
            if (year != null && StringUtils.isNotBlank(version)) {
                versionYears.merge(version, year, Math::min);
            }
        }
        List<Object[]> batchArgs = versionYears.entrySet().stream()
            .map(entry -> new Object[]{entry.getKey(), entry.getValue(), MARK})
            .toList();
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return batchArgs.size();
    }

    private int insert205Ef(List<Map<String, Object>> rows) {
        String sql = """
            INSERT INTO cv_electricity_factor_scope
            (scope_key, scope_name, sort_order, status, remark, create_time, update_time)
            VALUES (?, ?, 0, '0', ?, SYSDATETIME(), SYSDATETIME())
            """;
        List<Object[]> batchArgs = mapRows(rows, row -> {
            String key = text(row.get("因子口径Key"));
            String name = text(row.get("因子口径"));
            if (StringUtils.isBlank(key)) {
                return null;
            }
            return new Object[]{key, name, MARK};
        });
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return batchArgs.size();
    }

    private int insert206Gas(List<Map<String, Object>> rows) {
        String sql = """
            INSERT INTO cv_greenhouse_gas
            (gas_code, gas_name, sort_order, status, remark, create_time, update_time)
            VALUES (?, ?, ?, '0', ?, SYSDATETIME(), SYSDATETIME())
            """;
        List<Object[]> batchArgs = mapRows(rows, row -> {
            String key = text(row.get("GasKey"));
            String name = text(row.get("气体"));
            if (StringUtils.isBlank(key)) {
                return null;
            }
            return new Object[]{key, name, integer(row.get("排序")), MARK};
        });
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return batchArgs.size();
    }

    private List<Object[]> mapRows(List<Map<String, Object>> rows, java.util.function.Function<Map<String, Object>, Object[]> mapper) {
        return rows.stream()
            .map(mapper)
            .filter(arr -> arr != null)
            .toList();
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal && decimal.scale() <= 0) {
            return decimal.toPlainString();
        }
        return String.valueOf(value).trim();
    }

    private Integer integer(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String t = text(value);
        return StringUtils.isBlank(t) ? null : new BigDecimal(t).intValue();
    }

    private BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String t = text(value);
        return StringUtils.isBlank(t) ? null : new BigDecimal(t);
    }

    private static final class FactorData {
        private final List<Map<String, Object>> ef201Rows = new ArrayList<>();
        private final List<Map<String, Object>> ef202Rows = new ArrayList<>();
        private final List<Map<String, Object>> ef203Rows = new ArrayList<>();
        private final List<Map<String, Object>> ef204Rows = new ArrayList<>();
        private final List<Map<String, Object>> ef205Rows = new ArrayList<>();
        private final List<Map<String, Object>> gas206Rows = new ArrayList<>();
    }
}
