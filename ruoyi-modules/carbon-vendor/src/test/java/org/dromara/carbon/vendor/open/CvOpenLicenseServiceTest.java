package org.dromara.carbon.vendor.open;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.domain.CvSigningKey;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderResponse;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.renewal.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.license.mapper.CvSigningKeyMapper;
import org.dromara.carbon.vendor.license.service.CvLicensePrivateKeyProvider;
import org.dromara.carbon.vendor.openapi.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.openapi.service.impl.CvOpenLicenseServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
class CvOpenLicenseServiceTest {

    private CvLicenseIssueMapper licenseIssueMapper;
    private CvCustomerMapper customerMapper;
    private CvRenewalOrderMapper renewalOrderMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private ICvOpenApiAuditService openApiAuditService;
    private CvSigningKeyMapper signingKeyMapper;
    private CvLicensePrivateKeyProvider privateKeyProvider;
    private KeyPair keyPair;
    private CvOpenLicenseServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        renewalOrderMapper = mock(CvRenewalOrderMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        openApiAuditService = mock(ICvOpenApiAuditService.class);
        signingKeyMapper = mock(CvSigningKeyMapper.class);
        privateKeyProvider = mock(CvLicensePrivateKeyProvider.class);
        keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        when(signingKeyMapper.selectOne(any(), eq(false))).thenReturn(activeSigningKey());
        when(privateKeyProvider.resolvePrivateKeyPem(eq("env:UNIT_TEST_LICENSE_PRIVATE_KEY")))
            .thenReturn(signingKeyMaterial());
        when(tenantPackageMapper.selectById(eq(1001L))).thenReturn(activePackage());
        service = new CvOpenLicenseServiceImpl(
            licenseIssueMapper,
            customerMapper,
            renewalOrderMapper,
            tenantPackageMapper,
            openApiAuditService,
            signingKeyMapper,
            privateKeyProvider,
            new ObjectMapper()
        );
    }

    @Test
    void returnsActiveLicensePayloadForMatchingInstall() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());

        CvOpenLicenseCurrentResponse response = service.currentLicense(currentRequest());

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals(1001L, response.getCustomerId());
        assertEquals("active", response.getStatus());
        assertEquals("专业版", response.getPackageName());
        assertEquals("专业版", response.getEdition());
        assertEquals("factor-sync,report-template-sync", response.getFeatureCodes());
        assertTrue(response.getLicensePayload().contains("\"installId\":\"INSTALL-001\""));
        assertTrue(response.getLicensePayload().contains("\"keyId\":\"KEY-001\""));
        assertTrue(response.getSignatureText().length() > 0);
        verify(licenseIssueMapper).updateById(any(CvLicenseIssue.class));
        verify(openApiAuditService).recordSuccess(
            eq("/open/licenses/current"), eq("POST"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L),
            eq("keyId=KEY-001;currentSummary=local-cache"));
    }

    @Test
    void currentLicenseDoesNotTrustLegacyPackageSnapshotWithoutPackageId() {
        CvLicenseIssue legacy = activeLicense();
        legacy.setPackageId(null);
        legacy.setPackageName("Enterprise Plan");
        legacy.setEdition("standard");
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(legacy);

        CvOpenLicenseCurrentResponse response = service.currentLicense(currentRequest());

        assertNull(response.getPackageId());
        assertEquals("套餐未配置", response.getPackageName());
        assertEquals("套餐未配置", response.getEdition());
        assertTrue(response.getLicensePayload().contains("\"packageName\":\"套餐未配置\""));
        assertTrue(response.getLicensePayload().contains("\"edition\":\"套餐未配置\""));
        verify(licenseIssueMapper).updateById(any(CvLicenseIssue.class));
    }

    @Test
    void hidesPayloadForExpiredLicenseStatus() {
        CvLicenseIssue expired = activeLicense();
        expired.setValidTo(Date.from(Instant.now().minusSeconds(60)));
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(expired);

        CvOpenLicenseCurrentResponse response = service.currentLicense(currentRequest());

        assertEquals("expired", response.getStatus());
        assertNull(response.getLicensePayload());
        assertNull(response.getSignatureText());
    }

    @Test
    void rejectsCurrentLicenseInstallMismatchAndAuditsFailure() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvOpenLicenseCurrentRequest request = currentRequest();
        request.setInstallId("OTHER-INSTALL");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.currentLicense(request));

        assertEquals("授权文件的部署指纹与本机不匹配", exception.getMessage());
        verify(openApiAuditService).recordFailure(
            eq("/open/licenses/current"), eq("POST"), eq("LIC-001"), eq("OTHER-INSTALL"), isNull(),
            eq("keyId=KEY-001;currentSummary=local-cache"), eq("授权文件的部署指纹与本机不匹配"));
    }

    @Test
    void createsPendingManualRenewalOrderInVendorDatabaseOnly() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(customerMapper.selectById(eq(1001L))).thenReturn(activeCustomer());
        when(renewalOrderMapper.insert(any(CvRenewalOrder.class))).thenReturn(1);

        CvOpenRenewalOrderResponse response = service.createRenewalOrder(renewalRequest());

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).insert(orderCaptor.capture());
        CvRenewalOrder order = orderCaptor.getValue();
        assertTrue(order.getOrderNo().startsWith("REN-"));
        assertEquals(1001L, order.getCustomerId());
        assertEquals("LIC-001", order.getLicenseId());
        assertEquals("pending", order.getOrderStatus());
        assertEquals("manual", order.getPayChannel());
        assertEquals(BigDecimal.ZERO, order.getAmount());
        assertEquals("INSTALL-001", order.getInstallId());
        assertEquals(1001L, order.getRequestedPackageId());
        assertEquals("专业版", order.getRequestedPackageName());
        assertEquals("专业版", order.getRequestedEdition());
        assertEquals("P1Y", order.getRenewalPeriod());
        assertEquals("Ops", order.getContactName());
        assertEquals("ops@example.com", order.getContactEmail());
        assertEquals("13800000000", order.getContactPhone());
        assertEquals("IDEMP-001", order.getIdempotencyKey());
        assertEquals("open-api", order.getRequestSource());
        assertEquals(order.getOrderNo(), response.getOrderNo());
        assertEquals("renewal order created for manual processing", response.getMessage());
        verify(openApiAuditService).recordSuccess(
            eq("/open/renewal-orders"), eq("POST"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L),
            eq("edition=standard;packageId=;renewalPeriod=P1Y;contactName=Ops;contactEmail=ops@example.com;contactPhone=13800000000;idempotencyKey=IDEMP-001"));
    }

    @Test
    void reusesExistingRenewalOrderForMatchingIdempotencyKey() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        when(customerMapper.selectById(eq(1001L))).thenReturn(activeCustomer());
        CvRenewalOrder existing = new CvRenewalOrder();
        existing.setOrderNo("REN-EXISTING");
        existing.setCustomerId(1001L);
        existing.setLicenseId("LIC-001");
        existing.setInstallId("INSTALL-001");
        existing.setOrderStatus("pending");
        existing.setPayChannel("manual");
        existing.setAmount(BigDecimal.ZERO);
        when(renewalOrderMapper.selectOne(any(), eq(false))).thenReturn(existing);

        CvOpenRenewalOrderResponse response = service.createRenewalOrder(renewalRequest());

        assertEquals("REN-EXISTING", response.getOrderNo());
        assertTrue(response.isReused());
        assertEquals("renewal order already exists", response.getMessage());
        verify(renewalOrderMapper, never()).insert(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsRenewalForDisabledCustomerAndDoesNotCreateOrder() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());
        CvCustomer customer = activeCustomer();
        customer.setCustomerStatus("disabled");
        when(customerMapper.selectById(eq(1001L))).thenReturn(customer);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createRenewalOrder(renewalRequest()));

        assertEquals("Disabled customer cannot create renewal metadata", exception.getMessage());
        verify(renewalOrderMapper, never()).insert(any(CvRenewalOrder.class));
        verify(openApiAuditService).recordFailure(
            eq("/open/renewal-orders"), eq("POST"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L),
            eq("edition=standard;packageId=;renewalPeriod=P1Y;contactName=Ops;contactEmail=ops@example.com;contactPhone=13800000000;idempotencyKey=IDEMP-001"),
            eq("Disabled customer cannot create renewal metadata"));
    }

    private CvOpenLicenseCurrentRequest currentRequest() {
        CvOpenLicenseCurrentRequest request = new CvOpenLicenseCurrentRequest();
        request.setLicenseId("LIC-001");
        request.setInstallId("INSTALL-001");
        request.setKeyId("KEY-001");
        request.setCurrentSummary("local-cache");
        return request;
    }

    private CvOpenRenewalOrderRequest renewalRequest() {
        CvOpenRenewalOrderRequest request = new CvOpenRenewalOrderRequest();
        request.setLicenseId("LIC-001");
        request.setInstallId("INSTALL-001");
        request.setEdition("standard");
        request.setRenewalPeriod("P1Y");
        request.setContactName("Ops");
        request.setContactEmail("ops@example.com");
        request.setContactPhone("13800000000");
        request.setIdempotencyKey("IDEMP-001");
        return request;
    }

    private CvLicenseIssue activeLicense() {
        CvLicenseIssue license = new CvLicenseIssue();
        license.setLicenseId("LIC-001");
        license.setCustomerId(1001L);
        license.setPackageId(1001L);
        license.setPackageName("Enterprise Plan");
        license.setKeyId("KEY-001");
        license.setAlgorithm("Ed25519");
        license.setSchemaVersion("1");
        license.setEdition("standard");
        license.setFeatureCodes("factor-sync,report-template-sync");
        license.setInstallId("INSTALL-001");
        license.setIssueStatus("issued");
        license.setValidFrom(Date.from(Instant.now().minusSeconds(3600)));
        license.setValidTo(Date.from(Instant.now().plusSeconds(3600)));
        license.setLicensePayload("{\"licenseId\":\"LIC-001\"}");
        license.setSignatureText("signature-text");
        return license;
    }

    private SysTenantPackage activePackage() {
        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1001L);
        tenantPackage.setPackageName("专业版");
        tenantPackage.setStatus("0");
        tenantPackage.setDelFlag("0");
        return tenantPackage;
    }

    private CvSigningKey activeSigningKey() {
        CvSigningKey signingKey = new CvSigningKey();
        signingKey.setKeyId("KEY-001");
        signingKey.setAlgorithm("RS256");
        signingKey.setPrivateKeyRef("env:UNIT_TEST_LICENSE_PRIVATE_KEY");
        signingKey.setKeyStatus("active");
        signingKey.setValidFrom(Date.from(Instant.now().minusSeconds(3600)));
        return signingKey;
    }

    private String signingKeyMaterial() {
        return Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
            .encodeToString(keyPair.getPrivate().getEncoded());
    }

    private CvCustomer activeCustomer() {
        CvCustomer customer = new CvCustomer();
        customer.setId(1001L);
        customer.setCustomerCode("CUST-001");
        customer.setCustomerName("Test Manufacturing Co");
        customer.setCustomerStatus("active");
        return customer;
    }
}
