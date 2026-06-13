package org.dromara.carbon.vendor.open;

import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.CvReportTemplateDownloadToken;
import org.dromara.carbon.vendor.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateDownloadResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateRequest;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateDownloadTokenMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateMapper;
import org.dromara.carbon.vendor.mapper.CvReportTemplateScopeMapper;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.impl.CvOpenReportTemplateServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvOpenReportTemplateServiceTest {

    private CvLicenseIssueMapper licenseIssueMapper;
    private CvReportTemplateMapper reportTemplateMapper;
    private CvReportTemplateScopeMapper reportTemplateScopeMapper;
    private CvReportTemplateDownloadTokenMapper downloadTokenMapper;
    private ICvOpenApiAuditService openApiAuditService;
    private CvOpenReportTemplateServiceImpl service;

    @TempDir
    private Path templateRoot;

    @BeforeEach
    void setUp() throws Exception {
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        reportTemplateMapper = mock(CvReportTemplateMapper.class);
        reportTemplateScopeMapper = mock(CvReportTemplateScopeMapper.class);
        downloadTokenMapper = mock(CvReportTemplateDownloadTokenMapper.class);
        openApiAuditService = mock(ICvOpenApiAuditService.class);
        service = new CvOpenReportTemplateServiceImpl(
            licenseIssueMapper,
            reportTemplateMapper,
            reportTemplateScopeMapper,
            downloadTokenMapper,
            openApiAuditService
        );
        setReportTemplateRoot();
    }

    @Test
    void listsPublishedTemplatesAuthorizedForLicense() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(reportTemplateScopeMapper.selectList(any())).thenReturn(List.of(
            scope(301L, "LIC-001"),
            scope(302L, null),
            scope(303L, "OTHER-LIC")
        ));
        when(reportTemplateMapper.selectByIds(anyCollection())).thenReturn(List.of(
            template(301L, "carbon-standard", "2026.1", "published"),
            template(302L, "carbon-basic", "2026.1", "published"),
            template(303L, "carbon-private", "2026.1", "published")
        ));

        CvOpenReportTemplateListResponse response = service.listTemplates(request());

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals(2, response.getTemplates().size());
        assertEquals("carbon-basic", response.getTemplates().get(0).getTemplateCode());
        assertEquals("basic.pbix", response.getTemplates().get(0).getFileName());
        assertEquals("carbon-standard", response.getTemplates().get(1).getTemplateCode());
        verify(openApiAuditService).recordSuccess(
            eq("/open/report-templates"), eq("GET"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L), eq("action=list"));
    }

    @Test
    void excludesDraftTemplatesFromOpenList() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(reportTemplateScopeMapper.selectList(any())).thenReturn(List.of(scope(301L, null)));
        when(reportTemplateMapper.selectByIds(anyCollection())).thenReturn(List.of(
            template(301L, "carbon-draft", "2026.1", "draft")
        ));

        CvOpenReportTemplateListResponse response = service.listTemplates(request());

        assertEquals(0, response.getTemplates().size());
    }

    @Test
    void includesLicenseOnlyTemplateScopeWhenLicenseMatches() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvReportTemplateScope scope = scope(301L, "LIC-001");
        scope.setCustomerId(null);
        when(reportTemplateScopeMapper.selectList(any())).thenReturn(List.of(scope));
        when(reportTemplateMapper.selectByIds(anyCollection())).thenReturn(List.of(
            template(301L, "carbon-license", "2026.1", "published")
        ));

        CvOpenReportTemplateListResponse response = service.listTemplates(request());

        assertEquals(1, response.getTemplates().size());
        assertEquals("carbon-license", response.getTemplates().get(0).getTemplateCode());
    }

    @Test
    void rejectsCustomerAndLicenseScopeWhenCustomerDoesNotMatch() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvReportTemplateScope scope = scope(301L, "LIC-001");
        scope.setCustomerId(2002L);
        when(reportTemplateScopeMapper.selectList(any())).thenReturn(List.of(scope));

        CvOpenReportTemplateListResponse response = service.listTemplates(request());

        assertEquals(0, response.getTemplates().size());
        verify(reportTemplateMapper, never()).selectByIds(anyCollection());
    }

    @Test
    void returnsDownloadTokenOnlyAfterAuthorization() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(reportTemplateScopeMapper.selectList(any())).thenReturn(List.of(scope(301L, "LIC-001")));
        when(reportTemplateMapper.selectByIds(anyCollection())).thenReturn(List.of(
            template(301L, "carbon-standard", "2026.1", "published")
        ));

        CvOpenReportTemplateDownloadResponse response = service.downloadTemplate(301L, request());

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals(301L, response.getTemplateId());
        assertEquals(null, response.getFileUri());
        ArgumentCaptor<CvReportTemplateDownloadToken> tokenCaptor = ArgumentCaptor.forClass(CvReportTemplateDownloadToken.class);
        verify(downloadTokenMapper).insert(tokenCaptor.capture());
        assertEquals(response.getDownloadToken(), tokenCaptor.getValue().getDownloadToken());
        assertEquals("vendor://templates/carbon-standard.pbix", tokenCaptor.getValue().getFileUri());
        verify(openApiAuditService).recordSuccess(
            eq("/open/report-templates/{id}/download"), eq("GET"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L), eq("templateId=301"));
    }

    @Test
    void consumesDownloadTokenOnceAndStreamsTemplateFile() throws Exception {
        Path templateFile = templateRoot.resolve("templates/carbon-standard.pbix");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, "template-bytes");
        CvReportTemplateDownloadToken token = token("TOKEN-001", "vendor://templates/carbon-standard.pbix", "issued");
        when(downloadTokenMapper.selectOne(any(), eq(false))).thenReturn(token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.consumeDownloadToken("TOKEN-001", response);

        assertEquals("template-bytes", response.getContentAsString());
        ArgumentCaptor<CvReportTemplateDownloadToken> tokenCaptor = ArgumentCaptor.forClass(CvReportTemplateDownloadToken.class);
        verify(downloadTokenMapper).updateById(tokenCaptor.capture());
        assertEquals("consumed", tokenCaptor.getValue().getTokenStatus());
        verify(openApiAuditService).recordSuccess(
            eq("/open/report-templates/download-tokens/{token}"), eq("GET"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L), any());
    }

    @Test
    void rejectsDownloadForTemplateOutsideLicenseScope() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(reportTemplateScopeMapper.selectList(any())).thenReturn(List.of(scope(301L, "OTHER-LIC")));

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.downloadTemplate(301L, request()));

        assertEquals("report template is not authorized for license entitlement", exception.getMessage());
        verify(reportTemplateMapper, never()).selectByIds(anyCollection());
    }

    @Test
    void rejectsInstallIdMismatch() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvOpenReportTemplateRequest request = request();
        request.setInstallId("OTHER-INSTALL");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.listTemplates(request));

        assertEquals("license installId does not match", exception.getMessage());
        verify(reportTemplateScopeMapper, never()).selectList(any());
    }

    @Test
    void rejectsLicenseWithoutReportTemplateFeature() {
        CvLicenseIssue license = activeLicense();
        license.setFeatureCodes("factor-sync");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.listTemplates(request()));

        assertEquals("license feature is not authorized", exception.getMessage());
        verify(reportTemplateScopeMapper, never()).selectList(any());
    }

    @Test
    void rejectsExpiredLicense() {
        CvLicenseIssue license = activeLicense();
        license.setValidTo(Date.from(Instant.now().minusSeconds(60)));
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.listTemplates(request()));

        assertEquals("license entitlement is not currently valid", exception.getMessage());
        verify(reportTemplateScopeMapper, never()).selectList(any());
    }

    @Test
    void rejectsFutureLicense() {
        CvLicenseIssue license = activeLicense();
        license.setValidFrom(Date.from(Instant.now().plusSeconds(60)));
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.listTemplates(request()));

        assertEquals("license entitlement is not currently valid", exception.getMessage());
        verify(reportTemplateScopeMapper, never()).selectList(any());
    }

    @Test
    void rejectsRevokedLicense() {
        CvLicenseIssue license = activeLicense();
        license.setIssueStatus("revoked");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(license);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.listTemplates(request()));

        assertEquals("license entitlement is revoked", exception.getMessage());
        verify(reportTemplateScopeMapper, never()).selectList(any());
    }

    private CvOpenReportTemplateRequest request() {
        CvOpenReportTemplateRequest request = new CvOpenReportTemplateRequest();
        request.setLicenseId("LIC-001");
        request.setInstallId("INSTALL-001");
        return request;
    }

    private void setReportTemplateRoot() throws Exception {
        Field field = CvOpenReportTemplateServiceImpl.class.getDeclaredField("reportTemplateRoot");
        field.setAccessible(true);
        field.set(service, templateRoot.toString());
    }

    private CvReportTemplateDownloadToken token(String value, String fileUri, String status) {
        CvReportTemplateDownloadToken token = new CvReportTemplateDownloadToken();
        token.setDownloadToken(value);
        token.setLicenseId("LIC-001");
        token.setInstallId("INSTALL-001");
        token.setCustomerId(1001L);
        token.setTemplateId(301L);
        token.setFileName("carbon-standard.pbix");
        token.setFileUri(fileUri);
        token.setTokenStatus(status);
        token.setExpiresTime(Date.from(Instant.now().plusSeconds(60)));
        return token;
    }

    private CvLicenseIssue activeLicense() {
        CvLicenseIssue license = new CvLicenseIssue();
        license.setLicenseId("LIC-001");
        license.setCustomerId(1001L);
        license.setFeatureCodes("report-template-sync");
        license.setInstallId("INSTALL-001");
        license.setIssueStatus("issued");
        license.setValidFrom(Date.from(Instant.now().minusSeconds(3600)));
        license.setValidTo(Date.from(Instant.now().plusSeconds(3600)));
        return license;
    }

    private CvReportTemplateScope scope(Long templateId, String licenseId) {
        CvReportTemplateScope scope = new CvReportTemplateScope();
        scope.setTemplateId(templateId);
        scope.setCustomerId(1001L);
        scope.setLicenseId(licenseId);
        scope.setScopeStatus("enabled");
        return scope;
    }

    private CvReportTemplate template(Long id, String templateCode, String templateVersion, String publishStatus) {
        CvReportTemplate template = new CvReportTemplate();
        template.setId(id);
        template.setTemplateCode(templateCode);
        template.setTemplateName(templateCode + " name");
        template.setTemplateVersion(templateVersion);
        template.setFileName(templateCode.replace("carbon-", "") + ".pbix");
        template.setFileUri("vendor://templates/" + templateCode + ".pbix");
        template.setPublishStatus(publishStatus);
        template.setPublishedTime(Date.from(Instant.now()));
        return template;
    }
}
