package org.dromara.carbon.vendor.license;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssueResult;
import org.dromara.carbon.vendor.license.domain.CvLicenseRevokeRequest;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvLicenseIssueServiceTest {

    private static final Date ISSUED_AT = Date.from(Instant.parse("2026-06-04T00:00:00Z"));
    private static final Date VALID_FROM = Date.from(Instant.parse("2026-06-01T00:00:00Z"));
    private static final Date VALID_TO = Date.from(Instant.parse("2027-06-01T00:00:00Z"));

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
        when(licenseIssueMapper.selectList(any())).thenReturn(List.of());
    }

    @Test
    void issuesManualLicenseAndPersistsIssueRecord() throws Exception {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.issueManualLicense(validRequest());

        assertTrue(result.isIssued());
        assertEquals("ISSUED", result.getStatus());
        assertNotNull(result.getLicenseContent());

        ArgumentCaptor<CvLicenseIssue> issueCaptor = ArgumentCaptor.forClass(CvLicenseIssue.class);
        verify(licenseIssueMapper).insert(issueCaptor.capture());
        CvLicenseIssue issue = issueCaptor.getValue();

        assertEquals("LIC-UNIT-001", issue.getLicenseId());
        assertEquals(1001L, issue.getCustomerId());
        assertEquals(1001L, issue.getPackageId());
        assertEquals("标准版", issue.getPackageName());
        assertEquals("标准版", issue.getEdition());
        assertEquals("test-key-2026-01", issue.getKeyId());
        assertEquals("RS256", issue.getAlgorithm());
        assertEquals("license.v1", issue.getSchemaVersion());
        assertEquals("[\"factor_api\",\"report_template\"]", issue.getFeatureCodes());
        assertEquals("issued", issue.getIssueStatus());
        assertEquals("manual", issue.getIssueType());
        assertEquals(issue.getLicensePayload(), result.getLicenseIssue().getLicensePayload());

        JsonNode envelope = objectMapper.readTree(result.getLicenseContent());
        JsonNode issuedPayload = envelope.get("payload");
        JsonNode persistedPayload = objectMapper.readTree(issue.getLicensePayload());
        assertEquals("license.v1", envelope.get("schemaVersion").asText());
        assertEquals("RS256", envelope.get("algorithm").asText());
        assertEquals("test-key-2026-01", envelope.get("keyId").asText());
        assertEquals("LIC-UNIT-001", issuedPayload.get("licenseId").asText());
        assertEquals("CUST-001", issuedPayload.get("customerId").asText());
        assertEquals("Test Manufacturing Co", issuedPayload.get("customerName").asText());
        assertEquals(1001L, issuedPayload.get("packageId").asLong());
        assertEquals("标准版", issuedPayload.get("packageName").asText());
        assertEquals("CUST-001", persistedPayload.get("customerId").asText());
        assertEquals("Test Manufacturing Co", persistedPayload.get("customerName").asText());
        assertTrue(verifySignature(issuedPayload.toString(), envelope.get("signature").asText()));
    }

    @Test
    void rejectsUnsupportedAlgorithmBeforeSigning() {
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseIssueRequest request = validRequest();
        request.setAlgorithm("HS256");

        CvLicenseIssueResult result = service.issueManualLicense(request);

        assertEquals("UNSUPPORTED_ALGORITHM", result.getStatus());
    }

    @Test
    void rejectsMissingPrivateKeyReferenceResolution() {
        CvLicenseIssueServiceImpl service = newService(null);

        CvLicenseIssueResult result = service.issueManualLicense(validRequest());

        assertEquals("PRIVATE_KEY_UNAVAILABLE", result.getStatus());
    }

    @Test
    void rejectsDisabledCustomerBeforeIssuing() {
        when(customerMapper.selectById(eq(1001L))).thenReturn(disabledCustomer());
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.issueManualLicense(validRequest());

        assertFalse(result.isIssued());
        assertEquals("CUSTOMER_DISABLED", result.getStatus());
        assertNull(result.getLicenseContent());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void rejectsNumericDisabledCustomerBeforeIssuing() {
        CvCustomer customer = activeCustomer();
        customer.setCustomerStatus("1");
        when(customerMapper.selectById(eq(1001L))).thenReturn(customer);
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.issueManualLicense(validRequest());

        assertFalse(result.isIssued());
        assertEquals("CUSTOMER_DISABLED", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void rejectsDuplicateIssueForSameCustomerInstallAndValidityWindow() {
        when(licenseIssueMapper.selectList(any())).thenReturn(List.of(existingIssuedRecord()));
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.issueManualLicense(validRequest());

        assertFalse(result.isIssued());
        assertEquals("DUPLICATE_LICENSE_ISSUE", result.getStatus());
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void blocksReissueWhenRevokedHistoryExistsForCustomerInstall() {
        when(licenseIssueMapper.selectList(any())).thenReturn(List.of(existingRevokedRecord()));
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());

        CvLicenseIssueResult result = service.issueManualLicense(validRequest());

        assertFalse(result.isIssued());
        assertEquals("REVOKED_LICENSE_REISSUE_BLOCKED", result.getStatus());
        assertTrue(result.getMessage().contains("manual review"));
        verify(licenseIssueMapper, never()).insert(any(CvLicenseIssue.class));
    }

    @Test
    void revokesIssuedLicenseWithAuditMetadata() {
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(existingIssuedRecord());
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseRevokeRequest request = new CvLicenseRevokeRequest();
        request.setLicenseId("LIC-EXISTING-001");
        request.setRevokedBy("vendor-auditor");
        request.setRevokedAt(Date.from(Instant.parse("2026-06-12T00:00:00Z")));
        request.setRevokeReason("contract cancelled");

        service.revokeLicense(request);

        ArgumentCaptor<CvLicenseIssue> issueCaptor = ArgumentCaptor.forClass(CvLicenseIssue.class);
        verify(licenseIssueMapper).updateById(issueCaptor.capture());
        CvLicenseIssue update = issueCaptor.getValue();
        assertEquals("revoked", update.getIssueStatus());
        assertEquals("vendor-auditor", update.getRevokedBy());
        assertEquals("contract cancelled", update.getRevokeReason());
        assertEquals(Date.from(Instant.parse("2026-06-12T00:00:00Z")), update.getRevokedTime());
    }

    @Test
    void rejectsDuplicateRevocationWithoutChangingAuditRecord() {
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(existingRevokedRecord());
        CvLicenseIssueServiceImpl service = newService(signingKeyMaterial());
        CvLicenseRevokeRequest request = new CvLicenseRevokeRequest();
        request.setLicenseId("LIC-EXISTING-001");

        assertThrows(RuntimeException.class, () -> service.revokeLicense(request));

        verify(licenseIssueMapper, never()).updateById(any(CvLicenseIssue.class));
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

    private CvLicenseIssueRequest validRequest() {
        CvTemplateEntitlement entitlement = new CvTemplateEntitlement();
        entitlement.setTemplateCode("PBI-CARBON-MAIN");
        entitlement.setTemplateVersion("2026.1");
        entitlement.setScope("enterprise");

        CvLicenseIssueRequest request = new CvLicenseIssueRequest();
        request.setCustomerId(1001L);
        request.setCustomerCode("FORGED-CUST-999");
        request.setCustomerName("Forged Customer Name");
        request.setKeyId("test-key-2026-01");
        request.setSchemaVersion("license.v1");
        request.setAlgorithm("RS256");
        request.setPackageId(1001L);
        request.setEdition("standard");
        request.setFeatures(List.of("factor_api", "report_template"));
        request.setInstallId("INSTALL-ENTERPRISE-001");
        request.setValidFrom(VALID_FROM);
        request.setValidTo(VALID_TO);
        request.setIssuedAt(ISSUED_AT);
        request.setIssuedBy("vendor-admin");
        request.setLicenseId("LIC-UNIT-001");
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

    private CvCustomer disabledCustomer() {
        CvCustomer customer = activeCustomer();
        customer.setCustomerStatus("disabled");
        return customer;
    }

    private SysTenantPackage activePackage() {
        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1001L);
        tenantPackage.setPackageName("标准版");
        tenantPackage.setStatus("0");
        tenantPackage.setDelFlag("0");
        return tenantPackage;
    }

    private CvLicenseIssue existingIssuedRecord() {
        CvLicenseIssue issue = new CvLicenseIssue();
        issue.setLicenseId("LIC-EXISTING-001");
        issue.setCustomerId(1001L);
        issue.setInstallId("INSTALL-ENTERPRISE-001");
        issue.setValidFrom(VALID_FROM);
        issue.setValidTo(VALID_TO);
        issue.setIssueStatus("issued");
        issue.setIssuedTime(ISSUED_AT);
        return issue;
    }

    private CvLicenseIssue existingRevokedRecord() {
        CvLicenseIssue issue = existingIssuedRecord();
        issue.setIssueStatus("revoked");
        issue.setRevokedTime(Date.from(Instant.parse("2026-06-10T00:00:00Z")));
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
