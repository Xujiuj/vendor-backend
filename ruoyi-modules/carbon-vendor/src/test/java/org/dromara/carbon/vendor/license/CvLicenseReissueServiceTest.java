package org.dromara.carbon.vendor.license;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueResult;
import org.dromara.carbon.vendor.license.domain.CvLicenseReissueRequest;
import org.dromara.carbon.vendor.license.domain.CvTemplateEntitlement;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.license.mapper.CvSigningKeyMapper;
import org.dromara.carbon.vendor.license.service.CvLicensePrivateKeyProvider;
import org.dromara.carbon.vendor.license.service.impl.CvLicenseIssueServiceImpl;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvLicenseReissueServiceTest {

    private static final Date ISSUED_AT = Date.from(Instant.parse("2026-06-04T00:00:00Z"));
    private static final Date VALID_FROM = Date.from(Instant.parse("2026-06-15T00:00:00Z"));
    private static final Date VALID_TO = Date.from(Instant.parse("2027-06-15T00:00:00Z"));

    private ObjectMapper objectMapper;
    private CvCustomerMapper customerMapper;
    private CvLicenseIssueMapper licenseIssueMapper;
    private CvSigningKeyMapper signingKeyMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        customerMapper = mock(CvCustomerMapper.class);
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        signingKeyMapper = mock(CvSigningKeyMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        when(signingKeyMapper.selectOne(any(), any(Boolean.class))).thenReturn(activeSigningKey());
        when(customerMapper.selectById(eq(1001L))).thenReturn(activeCustomer());
        when(tenantPackageMapper.selectById(eq(1001L))).thenReturn(activePackage());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(revokedSourceIssue(), null);
        when(licenseIssueMapper.selectList(any())).thenReturn(List.of(revokedSourceIssue()));
        when(licenseIssueMapper.selectCount(any())).thenReturn(0L);
    }

    @Test
    void reissuesRevokedLicenseWithAppendOnlyRecordAndSameInstallIdByDefault() throws Exception {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseReissueRequest request = validReissueRequest();
        request.setCustomerCode("FORGED-CUST-999");
        request.setCustomerName("Forged Customer Name");

        CvLicenseIssueResult result = service.reissueRevokedLicense(request);

        assertTrue(result.isIssued());
        assertNotNull(result.getLicenseContent());

        ArgumentCaptor<CvLicenseIssue> issueCaptor = ArgumentCaptor.forClass(CvLicenseIssue.class);
        verify(licenseIssueMapper).insert(issueCaptor.capture());
        CvLicenseIssue issue = issueCaptor.getValue();

        assertEquals("LIC-REISSUE-001", issue.getLicenseId());
        assertEquals(1001L, issue.getPackageId());
        assertEquals("Enterprise Plan", issue.getPackageName());
        assertEquals("reissue", issue.getIssueType());
        assertEquals("LIC-REVOKED-001", issue.getSourceLicenseId());
        assertEquals("INSTALL-ENTERPRISE-001", issue.getInstallId());
        assertEquals("issued", issue.getIssueStatus());

        JsonNode envelope = objectMapper.readTree(result.getLicenseContent());
        JsonNode payload = envelope.get("payload");
        assertEquals("LIC-REISSUE-001", payload.get("licenseId").asText());
        assertEquals("CUST-001", payload.get("customerId").asText());
        assertEquals("Test Manufacturing Co", payload.get("customerName").asText());
        assertEquals(1001L, payload.get("packageId").asLong());
        assertEquals("Enterprise Plan", payload.get("packageName").asText());
        assertEquals("INSTALL-ENTERPRISE-001", payload.get("installId").asText());
        assertTrue(verifySignature(payload.toString(), envelope.get("signature").asText()));
    }

    @Test
    void rejectsInstallIdChangeUnlessExplicitlyAllowed() {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseReissueRequest request = validReissueRequest();
        request.setTargetInstallId("INSTALL-ENTERPRISE-999");

        CvLicenseIssueResult result = service.reissueRevokedLicense(request);

        assertFalse(result.isIssued());
        assertEquals("INSTALL_ID_CHANGE_NOT_ALLOWED", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void allowsInstallIdChangeWhenExplicitlyApproved() {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseReissueRequest request = validReissueRequest();
        request.setAllowInstallIdChange(true);
        request.setTargetInstallId("INSTALL-ENTERPRISE-999");

        CvLicenseIssueResult result = service.reissueRevokedLicense(request);

        assertTrue(result.isIssued());
        ArgumentCaptor<CvLicenseIssue> issueCaptor = ArgumentCaptor.forClass(CvLicenseIssue.class);
        verify(licenseIssueMapper).insert(issueCaptor.capture());
        assertEquals("INSTALL-ENTERPRISE-999", issueCaptor.getValue().getInstallId());
    }

    @Test
    void rejectsNonRevokedSourceLicense() {
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(activeSourceIssue());
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.reissueRevokedLicense(validReissueRequest());

        assertFalse(result.isIssued());
        assertEquals("SOURCE_LICENSE_NOT_REVOKED", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void rejectsMissingSourceLicense() {
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(null);
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.reissueRevokedLicense(validReissueRequest());

        assertFalse(result.isIssued());
        assertEquals("SOURCE_LICENSE_NOT_FOUND", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void rejectsSourceCustomerMismatch() {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseReissueRequest request = validReissueRequest();
        request.setCustomerId(2002L);

        CvLicenseIssueResult result = service.reissueRevokedLicense(request);

        assertFalse(result.isIssued());
        assertEquals("SOURCE_LICENSE_CUSTOMER_MISMATCH", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void rejectsReusingSourceLicenseIdForReissue() {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseReissueRequest request = validReissueRequest();
        request.setLicenseId("LIC-REVOKED-001");

        CvLicenseIssueResult result = service.reissueRevokedLicense(request);

        assertFalse(result.isIssued());
        assertEquals("DUPLICATE_LICENSE_ID", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void rejectsReissueReplayFromSameRevokedSource() {
        when(licenseIssueMapper.selectCount(any())).thenReturn(1L);
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.reissueRevokedLicense(validReissueRequest());

        assertFalse(result.isIssued());
        assertEquals("SOURCE_LICENSE_ALREADY_REISSUED", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void treatsConcurrentSourceLicenseUniqueViolationAsReissueReplay() {
        when(licenseIssueMapper.insert(any(CvLicenseIssue.class)))
            .thenThrow(new DuplicateKeyException("uk_cv_license_reissue_source"));
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.reissueRevokedLicense(validReissueRequest());

        assertFalse(result.isIssued());
        assertEquals("SOURCE_LICENSE_ALREADY_REISSUED", result.getStatus());
        assertEquals("原撤销授权已重签，不能重复重签", result.getMessage());
    }

    @Test
    void rejectsDuplicateActiveIssueDuringReissue() {
        when(licenseIssueMapper.selectList(any())).thenReturn(List.of(existingReissueTarget()));
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.reissueRevokedLicense(validReissueRequest());

        assertFalse(result.isIssued());
        assertEquals("DUPLICATE_LICENSE_ISSUE", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void masksSigningFailuresDuringReissue() {
        CvLicenseIssueServiceImpl service = newService("not-a-private-key");

        CvLicenseIssueResult result = service.reissueRevokedLicense(validReissueRequest());

        assertFalse(result.isIssued());
        assertEquals("ISSUE_FAILED", result.getStatus());
        assertEquals("授权签发失败", result.getMessage());
        assertNull(result.getLicenseContent());
    }

    private CvLicenseIssueServiceImpl newService(String signingKeyMaterial) {
        CvLicensePrivateKeyProvider keyProvider = privateKeyRef -> signingKeyMaterial;
        return new CvLicenseIssueServiceImpl(
            licenseIssueMapper,
            customerMapper,
            signingKeyMapper,
            tenantPackageMapper,
            keyProvider,
            objectMapper
        );
    }

    private CvLicenseReissueRequest validReissueRequest() {
        CvTemplateEntitlement entitlement = new CvTemplateEntitlement();
        entitlement.setTemplateCode("PBI-CARBON-MAIN");
        entitlement.setTemplateVersion("2026.2");
        entitlement.setScope("enterprise");

        CvLicenseReissueRequest request = new CvLicenseReissueRequest();
        request.setSourceLicenseId("LIC-REVOKED-001");
        request.setCustomerId(1001L);
        request.setKeyId("test-key-2026-01");
        request.setSchemaVersion("license.v1");
        request.setAlgorithm("RS256");
        request.setPackageId(1001L);
        request.setEdition("Enterprise Plan");
        request.setFeatures(List.of("factor_api", "report_template"));
        request.setValidFrom(VALID_FROM);
        request.setValidTo(VALID_TO);
        request.setIssuedAt(ISSUED_AT);
        request.setIssuedBy("vendor-admin");
        request.setLicenseId("LIC-REISSUE-001");
        request.setTemplateEntitlements(List.of(entitlement));
        return request;
    }

    private CvSigningKey activeSigningKey() {
        CvSigningKey signingKey = new CvSigningKey();
        signingKey.setKeyId("test-key-2026-01");
        signingKey.setAlgorithm("RS256");
        signingKey.setPrivateKeyRef("env:UNIT_TEST_LICENSE_PRIVATE_KEY");
        signingKey.setKeyStatus("active");
        signingKey.setValidFrom(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        return signingKey;
    }

    private CvCustomer activeCustomer() {
        CvCustomer customer = new CvCustomer();
        customer.setId(1001L);
        customer.setCustomerCode("CUST-001");
        customer.setCustomerName("Test Manufacturing Co");
        customer.setCustomerStatus("active");
        return customer;
    }

    private CvLicenseIssue revokedSourceIssue() {
        CvLicenseIssue issue = new CvLicenseIssue();
        issue.setLicenseId("LIC-REVOKED-001");
        issue.setCustomerId(1001L);
        issue.setPackageId(1001L);
        issue.setPackageName("Enterprise Plan");
        issue.setInstallId("INSTALL-ENTERPRISE-001");
        issue.setValidFrom(Date.from(Instant.parse("2025-06-01T00:00:00Z")));
        issue.setValidTo(Date.from(Instant.parse("2026-06-01T00:00:00Z")));
        issue.setIssueStatus("revoked");
        issue.setIssueType("manual");
        issue.setRevokedTime(Date.from(Instant.parse("2026-06-10T00:00:00Z")));
        return issue;
    }

    private CvLicenseIssue activeSourceIssue() {
        CvLicenseIssue issue = revokedSourceIssue();
        issue.setIssueStatus("issued");
        issue.setRevokedTime(null);
        return issue;
    }

    private SysTenantPackage activePackage() {
        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1001L);
        tenantPackage.setPackageName("Enterprise Plan");
        tenantPackage.setStatus("0");
        tenantPackage.setDelFlag("0");
        tenantPackage.setLicenseFeatureCodes("factor_api,report_template");
        tenantPackage.setLicenseTemplateEntitlements("[{\"templateCode\":\"TPL-001\",\"templateVersion\":\"v1\",\"scope\":\"download\"}]");
        return tenantPackage;
    }

    private CvLicenseIssue existingReissueTarget() {
        CvLicenseIssue issue = revokedSourceIssue();
        issue.setLicenseId("LIC-REISSUE-EXISTING");
        issue.setIssueStatus("issued");
        issue.setIssueType("reissue");
        issue.setSourceLicenseId("LIC-OTHER-REVOKED");
        issue.setRevokedTime(null);
        issue.setValidFrom(VALID_FROM);
        issue.setValidTo(VALID_TO);
        return issue;
    }

    private String signingKeyMaterial() {
        return Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
            .encodeToString(keyPair.getPrivate().getEncoded());
    }

    private boolean verifySignature(String canonicalPayload, String signatureText) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(keyPair.getPublic());
        signature.update(canonicalPayload.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.getDecoder().decode(signatureText));
    }
}
