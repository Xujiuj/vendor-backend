package org.dromara.carbon.vendor.open;

import org.dromara.carbon.vendor.domain.CvFactorRecord;
import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.mapper.CvFactorRecordMapper;
import org.dromara.carbon.vendor.mapper.CvFactorVersionMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvVendorTableFieldMapper;
import org.dromara.carbon.vendor.service.ICvFactorCustomerScopeService;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.impl.CvOpenFactorServiceImpl;
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
    private CvVendorTableFieldMapper tableFieldMapper;
    private ICvFactorCustomerScopeService factorCustomerScopeService;
    private ICvOpenApiAuditService openApiAuditService;
    private CvOpenFactorServiceImpl service;

    @BeforeEach
    void setUp() {
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        factorVersionMapper = mock(CvFactorVersionMapper.class);
        factorRecordMapper = mock(CvFactorRecordMapper.class);
        tableFieldMapper = mock(CvVendorTableFieldMapper.class);
        factorCustomerScopeService = mock(ICvFactorCustomerScopeService.class);
        openApiAuditService = mock(ICvOpenApiAuditService.class);
        service = new CvOpenFactorServiceImpl(
            licenseIssueMapper,
            factorVersionMapper,
            factorRecordMapper,
            tableFieldMapper,
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
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1001L, null, "standard", "LIC-001"))
            .thenReturn(true);
        when(factorRecordMapper.selectList(any())).thenReturn(List.of(factorRecord()));

        CvOpenFactorSyncResponse response = service.syncFactors(request("OLD"));

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals("88", response.getVendorVersionId());
        assertEquals("FV-2026", response.getVersionCode());
        assertTrue(response.isChanged());
        assertEquals(1, response.getRecords().size());
        assertEquals("EF-ELEC-ZJ", response.getRecords().get(0).getFactorCode());
        verify(openApiAuditService).recordSuccess(
            eq("/open/factors"), eq("GET"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L), eq("currentVersionCode=OLD"));
    }

    @Test
    void marksResponseUnchangedWhenVersionCodeMatchesRequest() {
        CvFactorVersion version = publishedVersion(88L, "FV-2026");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(factorVersionMapper.selectList(any())).thenReturn(List.of(version));
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1001L, null, "standard", "LIC-001"))
            .thenReturn(true);
        when(factorRecordMapper.selectList(any())).thenReturn(List.of(factorRecord()));

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
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1001L, null, "standard", "LIC-001"))
            .thenReturn(true);
        when(factorRecordMapper.selectList(any())).thenReturn(List.of(factorRecord()));

        CvOpenFactorSyncResponse response = service.syncFactors(request(null));

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals(1, response.getRecords().size());
    }

    @Test
    void rejectsInstallIdMismatch() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvOpenFactorSyncRequest request = request(null);
        request.setInstallId("OTHER-INSTALL");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.syncFactors(request));

        assertEquals("license installId does not match", exception.getMessage());
        verify(factorVersionMapper, never()).selectList(any());
        verify(openApiAuditService).recordFailure(
            eq("/open/factors"), eq("GET"), eq("LIC-001"), eq("OTHER-INSTALL"), isNull(),
            eq("currentVersionCode="), eq("license installId does not match"));
    }

    @Test
    void rejectsWhenNoAuthorizedFactorVersionExists() {
        CvFactorVersion version = publishedVersion(88L, "FV-2026");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(factorVersionMapper.selectList(any())).thenReturn(List.of(version));
        when(factorCustomerScopeService.isFactorVersionAuthorized(88L, 1001L, null, "standard", "LIC-001"))
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

    private CvFactorRecord factorRecord() {
        CvFactorRecord record = new CvFactorRecord();
        record.setId(701L);
        record.setVersionId(88L);
        record.setFactorCode("EF-ELEC-ZJ");
        record.setFactorName("Zhejiang grid electricity");
        record.setFactorCategory("electricity");
        record.setFactorValue(new BigDecimal("0.5703000000"));
        record.setFactorUnit("kgCO2e/kWh");
        record.setSourceRef("official-source");
        record.setEnabledFlag(Boolean.TRUE);
        return record;
    }
}
