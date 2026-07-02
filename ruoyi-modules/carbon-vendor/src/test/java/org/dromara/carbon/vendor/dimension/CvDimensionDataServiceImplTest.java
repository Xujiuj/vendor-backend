package org.dromara.carbon.vendor.dimension;

import org.dromara.carbon.vendor.dimension.domain.CvAdminDivision;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvAdminDivisionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvBaseYearMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvEmissionSourceCategoryMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.dimension.service.impl.CvDimensionDataServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvDimensionDataServiceImplTest {

    @Test
    void baseYearRecordFieldsMapToSourceAStrongFields() throws Exception {
        Map<String, Object> bo = new LinkedHashMap<>();
        bo.put("recordCode", "BY-2026");
        bo.put("recordName", "2026");
        bo.put("description", "2026 年基准年");
        bo.put("status", "0");
        bo.put("sortOrder", 1);

        applyRecordFields(bo);

        assertEquals("BY-2026", bo.get("baseYearKey"));
        assertEquals(2026, bo.get("baseYear"));
        assertEquals(1, bo.get("isCurrent"));
    }

    @Test
    void baseYearRejectsNonNumericRecordName() {
        Map<String, Object> bo = new LinkedHashMap<>();
        bo.put("recordCode", "BY-BAD");
        bo.put("recordName", "not-a-year");

        ServiceException exception = assertThrows(ServiceException.class, () -> applyRecordFields(bo));
        assertEquals("基准年份必须为数字", exception.getMessage());
    }

    @Test
    void baseYearRecordMapExposesBaseYearAsGenericRecordName() throws Exception {
        Map<String, Object> baseYear = new LinkedHashMap<>();
        baseYear.put("id", 8L);
        baseYear.put("base_year_key", "BY-2026");
        baseYear.put("base_year", 2026);
        baseYear.put("status", "0");

        Map<String, Object> record = toRecordMap(baseYear);

        assertEquals("BY-2026", record.get("recordCode"));
        assertEquals(2026, record.get("recordName"));
    }

    @Test
    void electricityVersionUsesSingleVersionFieldForCodeAndName() throws Exception {
        Map<String, Object> bo = new LinkedHashMap<>();
        bo.put("recordCode", "2023");
        bo.put("recordName", "should-not-overwrite");

        applyRecordFields("ef-electricity-version", bo);

        assertEquals("2023", bo.get("factorVersion"));
    }

    @Test
    void electricityFactorRecordFieldsUseVersionProvinceCodeAsGenericCode() throws Exception {
        Map<String, Object> bo = new LinkedHashMap<>();
        bo.put("recordCode", "2023-110000");
        bo.put("recordName", "北京市");

        applyRecordFields("ef-electricity-factor", bo);

        assertEquals("2023-110000", bo.get("versionProvinceCode"));
        assertEquals("北京市", bo.get("divisionName"));
    }

    @Test
    void electricityFactorRecordMapExposesVersionProvinceCodeAsGenericRecordCode() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 9L);
        row.put("version_province_code", "2023-110000");
        row.put("division_code", "110000");
        row.put("division_name", "北京市");
        row.put("status", "0");

        Map<String, Object> record = toRecordMap("ef-electricity-factor", row);

        assertEquals("2023-110000", record.get("recordCode"));
        assertEquals("北京市", record.get("recordName"));
        assertEquals("110000", record.get("divisionCode"));
    }

    @Test
    void electricityVersionInsertRejectsDuplicateVersionNumber() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM dbo.[cv_electricity_factor_version] WHERE [factor_version] = ?"),
            eq(Long.class),
            any(Object[].class)
        )).thenReturn(1L);
        Map<String, Object> bo = new LinkedHashMap<>();
        bo.put("recordCode", "2023");

        ServiceException exception = assertThrows(ServiceException.class, () ->
            service(mock(CvAdminDivisionMapper.class), mock(CvBaseYearMapper.class), mock(CvElectricityMapper.class), jdbcTemplate)
                .insertByBo("ef-electricity-version", bo));

        assertEquals("版本号已存在", exception.getMessage());
    }

    @Test
    void adminDivisionDeleteRejectsReferencedChildren() {
        CvAdminDivisionMapper adminDivisionMapper = mock(CvAdminDivisionMapper.class);
        CvElectricityMapper electricityMapper = mock(CvElectricityMapper.class);
        CvAdminDivision province = adminDivision(1L, "110000", null);
        when(adminDivisionMapper.selectList(any())).thenReturn(List.of(province));
        when(adminDivisionMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            service(adminDivisionMapper, mock(CvBaseYearMapper.class), electricityMapper)
                .deleteByIds("admin-division", List.of(1L)));

        assertEquals("行政区划存在下级区划引用，请先删除下级区划", exception.getMessage());
        verify(adminDivisionMapper, never()).deleteByIds(any());
    }

    @Test
    void adminDivisionDeleteRejectsReferencedElectricityFactors() {
        CvAdminDivisionMapper adminDivisionMapper = mock(CvAdminDivisionMapper.class);
        CvElectricityMapper electricityMapper = mock(CvElectricityMapper.class);
        CvAdminDivision province = adminDivision(1L, "110000", null);
        when(adminDivisionMapper.selectList(any())).thenReturn(List.of(province));
        when(adminDivisionMapper.selectCount(any())).thenReturn(0L);
        when(electricityMapper.selectCount(any())).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            service(adminDivisionMapper, mock(CvBaseYearMapper.class), electricityMapper)
                .deleteByIds("admin-division", List.of(1L)));

        assertEquals("行政区划已被电力因子引用，不能删除", exception.getMessage());
        verify(adminDivisionMapper, never()).deleteByIds(any());
    }

    @Test
    void adminDivisionDeleteAllowsUnreferencedRows() {
        CvAdminDivisionMapper adminDivisionMapper = mock(CvAdminDivisionMapper.class);
        CvElectricityMapper electricityMapper = mock(CvElectricityMapper.class);
        CvAdminDivision province = adminDivision(1L, "110000", null);
        when(adminDivisionMapper.selectList(any())).thenReturn(List.of(province));
        when(adminDivisionMapper.selectCount(any())).thenReturn(0L);
        when(electricityMapper.selectCount(any())).thenReturn(0L);
        when(adminDivisionMapper.deleteByIds(any())).thenReturn(1);

        int deleted = service(adminDivisionMapper, mock(CvBaseYearMapper.class), electricityMapper)
            .deleteByIds("admin-division", List.of(1L));

        assertEquals(1, deleted);
    }

    private void applyRecordFields(Map<String, Object> bo) throws Exception {
        applyRecordFields("base-year", bo);
    }

    private void applyRecordFields(String dimensionCode, Map<String, Object> bo) throws Exception {
        Method method = CvDimensionDataServiceImpl.class.getDeclaredMethod("applyRecordFields", String.class, Map.class);
        method.setAccessible(true);
        try {
            method.invoke(service(mock(CvBaseYearMapper.class)), dimensionCode, bo);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ServiceException serviceException) {
                throw serviceException;
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toRecordMap(Map<String, Object> baseYear) throws Exception {
        return toRecordMap("base-year", baseYear);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toRecordMap(String dimensionCode, Map<String, Object> row) throws Exception {
        Method method = CvDimensionDataServiceImpl.class.getDeclaredMethod("toRecordMap", String.class, Map.class);
        method.setAccessible(true);
        return (Map<String, Object>) method.invoke(service(mock(CvBaseYearMapper.class)), dimensionCode, row);
    }

    private CvDimensionDataServiceImpl service(CvBaseYearMapper baseYearMapper) {
        return service(mock(CvAdminDivisionMapper.class), baseYearMapper, mock(CvElectricityMapper.class));
    }

    private CvDimensionDataServiceImpl service(CvAdminDivisionMapper adminDivisionMapper,
                                               CvBaseYearMapper baseYearMapper,
                                               CvElectricityMapper electricityMapper) {
        return service(adminDivisionMapper, baseYearMapper, electricityMapper, mock(JdbcTemplate.class));
    }

    private CvDimensionDataServiceImpl service(CvAdminDivisionMapper adminDivisionMapper,
                                               CvBaseYearMapper baseYearMapper,
                                               CvElectricityMapper electricityMapper,
                                               JdbcTemplate jdbcTemplate) {
        return new CvDimensionDataServiceImpl(
            adminDivisionMapper,
            mock(CvEmissionSourceCategoryMapper.class),
            baseYearMapper,
            electricityMapper,
            mock(CvElectricityFactorVersionMapper.class),
            mock(CvElectricityFactorScopeMapper.class),
            mock(CvGreenhouseGasMapper.class),
            jdbcTemplate
        );
    }

    private CvAdminDivision adminDivision(Long id, String divisionCode, String parentCode) {
        CvAdminDivision division = new CvAdminDivision();
        division.setId(id);
        division.setDivisionCode(divisionCode);
        division.setParentCode(parentCode);
        return division;
    }
}
