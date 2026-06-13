package org.dromara.carbon.vendor.open;

import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.open.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenRenewalOrderResponse;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.service.ICvOpenApiAuditService;
import org.dromara.carbon.vendor.service.impl.CvOpenLicenseServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
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
    private ICvOpenApiAuditService openApiAuditService;
    private CvOpenLicenseServiceImpl service;

    @BeforeEach
    void setUp() {
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        renewalOrderMapper = mock(CvRenewalOrderMapper.class);
        openApiAuditService = mock(ICvOpenApiAuditService.class);
        service = new CvOpenLicenseServiceImpl(
            licenseIssueMapper,
            customerMapper,
            renewalOrderMapper,
            openApiAuditService
        );
    }

    @Test
    void returnsActiveLicensePayloadForMatchingInstall() {
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(activeLicense());

        CvOpenLicenseCurrentResponse response = service.currentLicense(currentRequest());

        assertEquals("LIC-001", response.getLicenseId());
        assertEquals(1001L, response.getCustomerId());
        assertEquals("active", response.getStatus());
        assertEquals("standard", response.getEdition());
        assertEquals("factor-sync,report-template-sync", response.getFeatureCodes());
        assertEquals("{\"licenseId\":\"LIC-001\"}", response.getLicensePayload());
        assertEquals("signature-text", response.getSignatureText());
        verify(openApiAuditService).recordSuccess(
            eq("/open/licenses/current"), eq("POST"), eq("LIC-001"), eq("INSTALL-001"), eq(1001L),
            eq("keyId=KEY-001;currentSummary=local-cache"));
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

        assertEquals("license installId does not match", exception.getMessage());
        verify(openApiAuditService).recordFailure(
            eq("/open/licenses/current"), eq("POST"), eq("LIC-001"), eq("OTHER-INSTALL"), isNull(),
            eq("keyId=KEY-001;currentSummary=local-cache"), eq("license installId does not match"));
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
        assertEquals("standard", order.getRequestedEdition());
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
            eq("edition=standard;renewalPeriod=P1Y;contactName=Ops;contactEmail=ops@example.com;contactPhone=13800000000;idempotencyKey=IDEMP-001"));
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
            eq("edition=standard;renewalPeriod=P1Y;contactName=Ops;contactEmail=ops@example.com;contactPhone=13800000000;idempotencyKey=IDEMP-001"),
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

    private CvCustomer activeCustomer() {
        CvCustomer customer = new CvCustomer();
        customer.setId(1001L);
        customer.setCustomerCode("CUST-001");
        customer.setCustomerName("Test Manufacturing Co");
        customer.setCustomerStatus("active");
        return customer;
    }
}
