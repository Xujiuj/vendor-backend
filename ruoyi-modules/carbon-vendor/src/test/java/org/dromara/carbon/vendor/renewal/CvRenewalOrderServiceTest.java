package org.dromara.carbon.vendor.renewal;

import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.bo.CvRenewalOrderBo;
import org.dromara.carbon.vendor.domain.renewal.CvRenewalCallbackRequest;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.service.impl.CvRenewalOrderServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvRenewalOrderServiceTest {

    private CvRenewalOrderMapper renewalOrderMapper;
    private CvCustomerMapper customerMapper;
    private CvLicenseIssueMapper licenseIssueMapper;
    private CvRenewalOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        renewalOrderMapper = mock(CvRenewalOrderMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        service = new CvRenewalOrderServiceImpl(renewalOrderMapper, customerMapper, licenseIssueMapper);

        when(customerMapper.selectById(eq(1001L))).thenReturn(activeCustomer());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(activeLicense("LIC-ORIGINAL-001"));
    }

    @Test
    void rejectsDisabledCustomerBeforeCreatingRenewalMetadata() {
        CvCustomer customer = activeCustomer();
        customer.setCustomerStatus("disabled");
        when(customerMapper.selectById(eq(1001L))).thenReturn(customer);

        assertThrows(ServiceException.class, () -> service.insertRenewalOrder(validOrderBo()));

        verify(renewalOrderMapper, never()).insert(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsRenewalMetadataForRevokedOriginalLicense() {
        CvLicenseIssue revoked = activeLicense("LIC-ORIGINAL-001");
        revoked.setIssueStatus("revoked");
        revoked.setRevokedTime(Date.from(Instant.parse("2026-06-15T00:00:00Z")));
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class))).thenReturn(revoked);

        assertThrows(ServiceException.class, () -> service.insertRenewalOrder(validOrderBo()));

        verify(renewalOrderMapper, never()).insert(any(CvRenewalOrder.class));
    }

    @Test
    void appliesPaymentCallbackAsVendorOrderAuthorizationMetadata() {
        when(renewalOrderMapper.selectOne(any(), eq(false))).thenReturn(existingOrder());
        when(licenseIssueMapper.selectOne(any(), any(Boolean.class)))
            .thenReturn(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));
        CvRenewalCallbackRequest callback = new CvRenewalCallbackRequest();
        callback.setOrderNo("REN-202606-001");
        callback.setOrderStatus("authorized");
        callback.setPayChannel("mock-pay");
        callback.setPaidTime(Date.from(Instant.parse("2026-06-20T00:00:00Z")));
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals(501L, update.getId());
        assertEquals("authorized", update.getOrderStatus());
        assertEquals("mock-pay", update.getPayChannel());
        assertEquals("LIC-RENEWED-001", update.getIssuedLicenseId());
        assertEquals(Date.from(Instant.parse("2026-06-20T00:00:00Z")), update.getPaidTime());
    }

    @Test
    void rejectsEmptyRenewalCallback() {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(null));

        assertEquals("Renewal callback metadata cannot be empty", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsCallbackThatOnlyReferencesOrderWithoutExplicitStatus() {
        when(renewalOrderMapper.selectOne(any(), eq(false))).thenReturn(existingOrder());
        CvRenewalCallbackRequest callback = new CvRenewalCallbackRequest();
        callback.setOrderNo("REN-202606-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Renewal callback must include explicit orderStatus", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsRepeatedCallbackAfterTerminalStatus() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("authorized");
        when(renewalOrderMapper.selectOne(any(), eq(false))).thenReturn(order);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("paid");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Renewal callback cannot overwrite terminal order status", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsPaidOrderCallbackOverwrite() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("paid");
        when(renewalOrderMapper.selectOne(any(), eq(false))).thenReturn(order);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(validPaidCallback()));

        assertEquals("Renewal callback cannot overwrite terminal order status", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    private CvRenewalOrderBo validOrderBo() {
        CvRenewalOrderBo bo = new CvRenewalOrderBo();
        bo.setOrderNo("REN-202606-001");
        bo.setCustomerId(1001L);
        bo.setLicenseId("LIC-ORIGINAL-001");
        bo.setAmount(new BigDecimal("12800.00"));
        return bo;
    }

    private CvRenewalCallbackRequest validPaidCallback() {
        CvRenewalCallbackRequest callback = new CvRenewalCallbackRequest();
        callback.setOrderNo("REN-202606-001");
        callback.setOrderStatus("paid");
        callback.setPayChannel("mock-pay");
        callback.setPaidTime(Date.from(Instant.parse("2026-06-20T00:00:00Z")));
        return callback;
    }

    private CvCustomer activeCustomer() {
        CvCustomer customer = new CvCustomer();
        customer.setId(1001L);
        customer.setCustomerCode("CUST-001");
        customer.setCustomerName("Test Manufacturing Co");
        customer.setCustomerStatus("active");
        return customer;
    }

    private CvLicenseIssue activeLicense(String licenseId) {
        CvLicenseIssue issue = new CvLicenseIssue();
        issue.setLicenseId(licenseId);
        issue.setCustomerId(1001L);
        issue.setIssueStatus("issued");
        return issue;
    }

    private CvRenewalOrder existingOrder() {
        CvRenewalOrder order = new CvRenewalOrder();
        order.setId(501L);
        order.setOrderNo("REN-202606-001");
        order.setCustomerId(1001L);
        order.setLicenseId("LIC-ORIGINAL-001");
        order.setPayChannel("mock-pay");
        order.setOrderStatus("pending");
        return order;
    }
}
