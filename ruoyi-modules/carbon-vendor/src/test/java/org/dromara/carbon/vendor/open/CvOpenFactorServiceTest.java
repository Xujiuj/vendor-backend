package org.dromara.carbon.vendor.open;

import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorScope;
import org.dromara.carbon.vendor.dimension.domain.CvElectricityFactorVersion;
import org.dromara.carbon.vendor.dimension.domain.CvGreenhouseGas;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorScopeMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityFactorVersionMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvElectricityMapper;
import org.dromara.carbon.vendor.dimension.mapper.CvGreenhouseGasMapper;
import org.dromara.carbon.vendor.factor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.factor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.factor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.factor.service.ICvFactorCustomerScopeService;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.impl.CvOpenFactorServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvOpenFactorServiceTest {

    private CvLicenseIssueMapper licenseIssueMapper;
    private CvFactorVersionMapper factorVersionMapper;
    private CvFactorRecordMapper factorRecordMapper;
    private CvElectricityMapper electricityMapper;
    private CvElectricityFactorVersionMapper electricityFactorVersionMapper;
    private CvElectricityFactorScopeMapper electricityFactorScopeMapper;
    private CvGreenhouseGasMapper greenhouseGasMapper;
    private ICvFactorCustomerScopeService factorCustomerScopeService;
    private ICvOpenApiAuditService openApiAuditService;
    private CvOpenFactorServiceImpl service;

    @BeforeEach
    void setUp() {
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        factorRecordMapper = mock(CvFactorRecordMapper.class);
        electricityMapper = mock(CvElectricityMapper.class);
        electricityFactorVersionMapper = mock(CvElectricityFactorVersionMapper.class);
        electricityFactorScopeMapper = mock(CvElectricityFactorScopeMapper.class);
        greenhouseGasMapper = mock(CvGreenhouseGasMapper.class);
        factorCustomerScopeService = mock(ICvFactorCustomerScopeService.class);
        openApiAuditService = mock(ICvOpenApiAuditService.class);
        service = new CvOpenFactorServiceImpl(
            licenseIssueMapper,
            factorVersionMapper,
            factorRecordMapper,
            electricityMapper,
            electricityFactorVersionMapper,
            electricityFactorScopeMapper,
            greenhouseGasMapper,
            factorCustomerScopeService,
            openApiAuditService
        );
    }

    @Test
    void returnsLatestAuthorizedFactorRecordsForLicense() {
        CvLicenseIssue license = activeLicense();
        CvFactorVersion version = publishedVersion(88L, "FV-2026");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);
        when(factorVersionMapper.selectList(any())).thenReturn(List.of(version));
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1002L))
            .thenReturn(true);
        mockSourceAFactors();

        CvOpenFactorSyncResponse response = service.syncFactors(request("OLD"));

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals("88", response.getVendorVersionId());
        assertEquals("FV-2026", response.getVersionCode());
        assertTrue(response.isChanged());
        assertEquals(5, response.getRecords().size());
        assertEquals("201ef", response.getRecords().get(0).getFactorTableCode());
        assertEquals("1", response.getRecords().get(0).getFactorKey());
        assertEquals("天然气", response.getRecords().get(0).getEmissionSourceName());
        assertEquals(new BigDecimal("7.9661807000"), response.getRecords().get(0).getFactorGwp());
        assertEquals("202ef", response.getRecords().get(1).getFactorTableCode());
        assertEquals("2026330000", response.getRecords().get(1).getFactorCode());
        assertEquals(new BigDecimal("0.5703000000"), response.getRecords().get(1).getProvinceFactor());
        assertEquals("203ef", response.getRecords().get(2).getFactorTableCode());
        assertEquals("205ef", response.getRecords().get(3).getFactorTableCode());
        assertEquals("206", response.getRecords().get(4).getFactorTableCode());
        verify(openApiAuditService).recordSuccess(
            eq("/open/factors"), eq("GET"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L), eq("currentVersionCode=OLD"));
    }

    @Test
    void marksResponseUnchangedWhenVersionCodeMatchesRequest() {
        CvFactorVersion version = publishedVersion(88L, "FV-2026");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(factorVersionMapper.selectList(any())).thenReturn(List.of(version));
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1002L))
            .thenReturn(true);
        mockSourceAFactors();

        CvOpenFactorSyncResponse response = service.syncFactors(request("FV-2026"));

        assertFalse(response.isChanged());
    }

    @Test
    void acceptsJsonArrayFeatureCodesFromIssuedLicensePayload() {
        CvLicenseIssue license = activeLicense();
        license.setFeatureCodes("[\"capture\",\"factor-sync\",\"report-template-download\"]");
        CvFactorVersion version = publishedVersion(88L, "FV-2026");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);
        when(factorVersionMapper.selectList(any())).thenReturn(List.of(version));
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1002L))
            .thenReturn(true);
        mockSourceAFactors();

        CvOpenFactorSyncResponse response = service.syncFactors(request(null));

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals(5, response.getRecords().size());
    }

    @Test
    void rejectsInstallIdMismatch() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvOpenFactorSyncRequest request = request(null);
        request.setInstallId("OTHER-INSTALL");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.syncFactors(request));

        assertEquals("授权文件的部署指纹与本机不匹配", exception.getMessage());
        verify(factorVersionMapper, never()).selectList(any());
        verify(openApiAuditService).recordFailure(
            eq("/open/factors"), eq("GET"), eq("LIC-001"), eq("OTHER-INSTALL"), isNull(),
            eq("currentVersionCode="), eq("授权文件的部署指纹与本机不匹配"));
    }

    @Test
    void rejectsWhenNoAuthorizedFactorVersionExists() {
        CvFactorVersion version = publishedVersion(88L, "FV-2026");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(factorVersionMapper.selectList(any())).thenReturn(List.of(version));
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1002L))
            .thenReturn(false);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.syncFactors(request(null)));

        assertEquals("no authorized factor version for license entitlement", exception.getMessage());
        verify(factorRecordMapper, never()).selectList(any());
    }

    @Test
    void rejectsLicenseWithoutFactorSyncFeature() {
        CvLicenseIssue license = activeLicense();
        license.setFeatureCodes("report-template-sync");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.syncFactors(request(null)));

        assertEquals("license feature is not authorized", exception.getMessage());
        verify(factorVersionMapper, never()).selectList(any());
    }

    @Test
    void rejectsExpiredLicense() {
        CvLicenseIssue license = activeLicense();
        license.setValidTo(Date.from(Instant.now().minusSeconds(60)));
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.syncFactors(request(null)));

        assertEquals("license entitlement is not currently valid", exception.getMessage());
        verify(factorVersionMapper, never()).selectList(any());
    }

    private CvOpenFactorSyncRequest request(String currentVersionCode) {
        CvOpenFactorSyncRequest request = new CvOpenFactorSyncRequest();
        request.setLicenseId("LIC-001");
        request.setInstallId("INSTALL-001");
        request.setCurrentVersionCode(currentVersionCode);
        return request;
    }

    private CvLicenseIssue activeLicense() {
        CvLicenseIssue license = new CvLicenseIssue();
        license.setLicenseId("LIC-001");
        license.setCustomerId(1001L);
        license.setPackageId(1002L);
        license.setPackageName("专业版");
        license.setEdition("standard");
        license.setFeatureCodes("factor-sync");
        license.setInstallId("INSTALL-001");
        license.setIssueStatus("issued");
        license.setValidFrom(Date.from(Instant.now().minusSeconds(3600)));
        license.setValidTo(Date.from(Instant.now().plusSeconds(3600)));
        return license;
    }

    private CvFactorVersion publishedVersion(Long id, String versionCode) {
        CvFactorVersion version = new CvFactorVersion();
        version.setId(id);
        version.setVersionCode(versionCode);
        version.setVersionName("2026 factors");
        version.setPublishStatus("published");
        version.setFrozenFlag(Boolean.FALSE);
        version.setPublishedTime(Date.from(Instant.now()));
        return version;
    }

    private void mockSourceAFactors() {
        when(factorRecordMapper.selectList(any())).thenReturn(List.of(factor201(), factor202()));
        when(electricityFactorVersionMapper.selectList(any())).thenReturn(List.of(electricityFactorVersion()));
        when(electricityFactorScopeMapper.selectList(any())).thenReturn(List.of(electricityFactorScope()));
        when(greenhouseGasMapper.selectList(any())).thenReturn(List.of(greenhouseGas()));
    }

    private CvFactorRecord factor201() {
        CvFactorRecord record = new CvFactorRecord();
        record.setVersionId(88L);
        record.setFactorTableCode("201ef");
        record.setFactorCode("1");
        record.setFactorName("天然气");
        record.setFactorCategory("化石燃料-气体");
        record.setFactorValue(new BigDecimal("7.9661807000"));
        record.setFactorUnit("kgCO2e/m³");
        record.setFactorKey("1");
        record.setEmissionSourceName("天然气");
        record.setEmissionSourceNameEn("Natural Gas");
        record.setFuelMaterialCategory("化石燃料-气体");
        record.setSourceUnit("m³");
        record.setCo2(new BigDecimal("2.0369492"));
        record.setCh4(new BigDecimal("0.178055"));
        record.setN2o(new BigDecimal("0.0035611"));
        record.setApplicableScope("范围1");
        record.setFactorSource("国家温室气体排放因子数据库");
        record.setGwpCh4(new BigDecimal("28"));
        record.setGwpN2o(new BigDecimal("265"));
        record.setGwpSf6(new BigDecimal("23500"));
        record.setGwpNf3(new BigDecimal("17200"));
        record.setFactorGwp(new BigDecimal("7.9661807000"));
        record.setRowNo(1);
        record.setGwpValue(new BigDecimal("7.9661807000"));
        record.setConvertedFactor(new BigDecimal("7.9661807000"));
        record.setSourceRef("201EF排放因子维度表");
        record.setEnabledFlag(Boolean.TRUE);
        record.setRemark("source(A)");
        return record;
    }

    private CvFactorRecord factor202() {
        CvFactorRecord record = new CvFactorRecord();
        record.setVersionId(88L);
        record.setFactorTableCode("202ef");
        record.setFactorCode("2026:330000");
        record.setFactorName("浙江省");
        record.setFactorCategory("ef-electricity-factor");
        record.setFactorValue(new BigDecimal("0.5703000000"));
        record.setFactorUnit("kgCO2e/kWh");
        record.setFactorKey("2026:330000");
        record.setEmissionSourceName("电力");
        record.setEmissionSourceNameEn("Electricity");
        record.setFuelMaterialCategory("电力");
        record.setSourceUnit("kWh");
        record.setCo2(new BigDecimal("0.5703000000"));
        record.setApplicableScope("电力因子");
        record.setFactorSource("source(A)");
        record.setFactorGwp(new BigDecimal("0.5703000000"));
        record.setVersionProvinceCode("2026330000");
        record.setFactorVersion("2026");
        record.setDivisionCode("330000");
        record.setDivisionName("浙江省");
        record.setProvinceFactor(new BigDecimal("0.5703000000"));
        record.setRowNo(2);
        record.setGwpValue(new BigDecimal("0.5703000000"));
        record.setConvertedFactor(new BigDecimal("0.5703000000"));
        record.setSourceRef("202EF电力因子维度表");
        record.setEnabledFlag(Boolean.TRUE);
        record.setRemark("source(A)");
        return record;
    }

    private CvElectricityFactorVersion electricityFactorVersion() {
        CvElectricityFactorVersion record = new CvElectricityFactorVersion();
        record.setFactorVersion("2026");
        record.setEffectiveYear(2026);
        return record;
    }

    private CvElectricityFactorScope electricityFactorScope() {
        CvElectricityFactorScope record = new CvElectricityFactorScope();
        record.setScopeKey("province");
        record.setScopeName("省级电网");
        return record;
    }

    private CvGreenhouseGas greenhouseGas() {
        CvGreenhouseGas record = new CvGreenhouseGas();
        record.setGasCode("CO2");
        record.setGasName("二氧化碳");
        record.setGwpValue(BigDecimal.ONE);
        record.setGwpVersion("AR6");
        return record;
    }
}
