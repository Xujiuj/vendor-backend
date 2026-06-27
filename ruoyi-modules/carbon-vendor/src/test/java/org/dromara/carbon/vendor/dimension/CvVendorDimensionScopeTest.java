package org.dromara.carbon.vendor.dimension;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.dimension.domain.bo.CvDimensionRecordBo;
import org.dromara.carbon.vendor.tablefield.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.dimension.mapper.CvDimensionRecordMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.tablefield.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvAdminDivisionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvBaseYearMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvEmissionSourceCategoryMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.dimension.service.impl.CvDimensionRecordServiceImpl;
import org.dromara.carbon.vendor.openapi.service.impl.CvOpenDimensionServiceImpl;
import org.dromara.carbon.vendor.tablefield.service.impl.CvVendorTableFieldServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvVendorDimensionScopeTest {

    @Test
    void dimensionRecordsRejectEnterpriseFilledDimensions() {
        CvDimensionRecordMapper mapper = mock(CvDimensionRecordMapper.class);
        CvDimensionRecordServiceImpl service = new CvDimensionRecordServiceImpl(mapper);

        assertThrows(ServiceException.class, () -> service.insertByBo(dimensionRecord("company")));
        assertThrows(ServiceException.class, () -> service.insertByBo(dimensionRecord("intensity-target")));
        verify(mapper, never()).selectVoPage(any(), any(Wrapper.class));
    }

    @Test
    void tableFieldDefinitionsRejectEnterpriseFilledDimensionTables() {
        CvVendorTableFieldMapper mapper = mock(CvVendorTableFieldMapper.class);
        CvVendorTableFieldServiceImpl service = new CvVendorTableFieldServiceImpl(mapper, mock(JdbcTemplate.class));

        assertThrows(ServiceException.class, () -> service.insertByBo(tableField("company")));
        assertThrows(ServiceException.class, () -> service.insertByBo(tableField("intensity-denominator")));
        assertThrows(ServiceException.class, () -> service.queryList(tableField("company")));
        verify(mapper, never()).selectCount(any());
    }

    @Test
    void tableFieldDefinitionsRejectEnterpriseFilledFactorTables() {
        CvVendorTableFieldMapper mapper = mock(CvVendorTableFieldMapper.class);
        CvVendorTableFieldServiceImpl service = new CvVendorTableFieldServiceImpl(mapper, mock(JdbcTemplate.class));

        assertThrows(ServiceException.class, () -> service.insertByBo(tableField("factor", "201ef")));
        assertThrows(ServiceException.class, () -> service.insertByBo(tableField("factor", "204ef")));
        assertThrows(ServiceException.class, () -> service.queryList(tableField("factor", "201ef")));
        verify(mapper, never()).selectCount(any());
    }

    @Test
    void openDimensionApiRejectsEnterpriseFilledDimensions() {
        CvLicenseIssueMapper licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        CvDimensionRecordMapper dimensionRecordMapper = mock(CvDimensionRecordMapper.class);
        ICvOpenApiAuditService auditService = mock(ICvOpenApiAuditService.class);
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvOpenDimensionServiceImpl service = new CvOpenDimensionServiceImpl(
            licenseIssueMapper,
            dimensionRecordMapper,
            mock(CvAdminDivisionMapper.class),
            mock(CvEmissionSourceCategoryMapper.class),
            mock(CvBaseYearMapper.class),
            mock(CvElectricityMapper.class),
            mock(CvElectricityFactorVersionMapper.class),
            mock(CvElectricityFactorScopeMapper.class),
            mock(CvGreenhouseGasMapper.class),
            auditService
        );

        assertThrows(ServiceException.class, () -> service.listDimensions(openRequest("company")));
        assertThrows(ServiceException.class, () -> service.listDimensions(openRequest("intensity-tolerance")));
        verify(dimensionRecordMapper, never()).selectPage(any(), any(Wrapper.class));
    }

    private CvDimensionRecordBo dimensionRecord(String dimensionCode) {
        CvDimensionRecordBo bo = new CvDimensionRecordBo();
        bo.setDimensionCode(dimensionCode);
        bo.setRecordCode("TEST-001");
        bo.setRecordName("测试维度");
        return bo;
    }

    private CvVendorTableFieldBo tableField(String tableCode) {
        return tableField("dimension", tableCode);
    }

    private CvVendorTableFieldBo tableField(String tableGroup, String tableCode) {
        CvVendorTableFieldBo bo = new CvVendorTableFieldBo();
        bo.setTableGroup(tableGroup);
        bo.setTableCode(tableCode);
        bo.setFieldKey("field01");
        bo.setFieldLabel("测试字段");
        bo.setFieldType("text");
        return bo;
    }

    private CvOpenDimensionRequest openRequest(String dimensionCode) {
        CvOpenDimensionRequest request = new CvOpenDimensionRequest();
        request.setLicenseId("LIC-001");
        request.setInstallId("INSTALL-001");
        request.setDimensionCode(dimensionCode);
        return request;
    }

    private CvLicenseIssue activeLicense() {
        CvLicenseIssue license = new CvLicenseIssue();
        license.setLicenseId("LIC-001");
        license.setInstallId("INSTALL-001");
        license.setCustomerId(1001L);
        license.setValidFrom(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        license.setValidTo(Date.from(Instant.parse("2027-01-01T00:00:00Z")));
        return license;
    }
}
