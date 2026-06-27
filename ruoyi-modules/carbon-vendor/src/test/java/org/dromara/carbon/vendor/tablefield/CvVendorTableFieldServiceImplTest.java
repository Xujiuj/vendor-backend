package org.dromara.carbon.vendor.tablefield;

import org.dromara.carbon.vendor.tablefield.domain.CvVendorTableField;
import org.dromara.carbon.vendor.tablefield.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.tablefield.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.tablefield.service.impl.CvVendorTableFieldServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvVendorTableFieldServiceImplTest {

    @Test
    void insertRejectsUnsafeColumnNames() {
        CvVendorTableFieldMapper mapper = mock(CvVendorTableFieldMapper.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        CvVendorTableFieldServiceImpl service = new CvVendorTableFieldServiceImpl(mapper, jdbcTemplate);

        CvVendorTableFieldBo bo = validBo();
        bo.setFieldKey("name;drop table");

        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
        verify(jdbcTemplate, never()).execute(any(String.class));
        verify(mapper, never()).insert(any(CvVendorTableField.class));
    }

    @Test
    void insertRequiresOptionsForSelectFields() {
        CvVendorTableFieldServiceImpl service = new CvVendorTableFieldServiceImpl(mock(CvVendorTableFieldMapper.class), mock(JdbcTemplate.class));
        CvVendorTableFieldBo bo = validBo();
        bo.setFieldType("select");
        bo.setFieldOptions("");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertByBo(bo));

        assertEquals("选项字段必须填写选项", exception.getMessage());
    }

    @Test
    void insertAddsPhysicalColumnWhenMissingBeforeMetadata() {
        CvVendorTableFieldMapper mapper = mock(CvVendorTableFieldMapper.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("cv_admin_division"), eq("custom_level"))).thenReturn(0);
        when(mapper.insert(any(CvVendorTableField.class))).thenReturn(1);
        CvVendorTableFieldServiceImpl service = new CvVendorTableFieldServiceImpl(mapper, jdbcTemplate);

        boolean inserted = service.insertByBo(validBo());

        assertEquals(true, inserted);
        verify(jdbcTemplate).execute("ALTER TABLE `cv_admin_division` ADD COLUMN `custom_level` VARCHAR(255) NULL COMMENT '自定义层级'");
        verify(mapper).insert(any(CvVendorTableField.class));
    }

    @Test
    void insertExistingPhysicalColumnOnlyCreatesMetadata() {
        CvVendorTableFieldMapper mapper = mock(CvVendorTableFieldMapper.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("cv_admin_division"), eq("custom_level"))).thenReturn(1);
        when(mapper.insert(any(CvVendorTableField.class))).thenReturn(1);
        CvVendorTableFieldServiceImpl service = new CvVendorTableFieldServiceImpl(mapper, jdbcTemplate);

        boolean inserted = service.insertByBo(validBo());

        assertEquals(true, inserted);
        verify(jdbcTemplate, never()).execute(any(String.class));
        verify(mapper).insert(any(CvVendorTableField.class));
    }

    private CvVendorTableFieldBo validBo() {
        CvVendorTableFieldBo bo = new CvVendorTableFieldBo();
        bo.setTableGroup("dimension");
        bo.setTableCode("admin-division");
        bo.setFieldKey("custom_level");
        bo.setFieldLabel("自定义层级");
        bo.setFieldType("text");
        bo.setStatus("0");
        bo.setRequiredFlag(false);
        return bo;
    }
}
