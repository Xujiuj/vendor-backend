package org.dromara.carbon.vendor.renewal;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.renewal.domain.bo.CvRenewalOrderBo;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalCallbackRequest;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.renewal.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.renewal.service.impl.CvRenewalOrderServiceImpl;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class CvRenewalOrderServiceTest {

    private CvRenewalOrderMapper renewalOrderMapper;
    private CvCustomerMapper customerMapper;
    private CvLicenseIssueMapper licenseIssueMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private CvRenewalOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        renewalOrderMapper = mock(CvRenewalOrderMapper.class);
        customerMapper = mock(CvCustomerMapper.class);
        licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        service = new CvRenewalOrderServiceImpl(renewalOrderMapper, customerMapper, licenseIssueMapper, tenantPackageMapper);

        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new Configuration(), CvLicenseIssueMapper.class.getName()),
            CvLicenseIssue.class);
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new Configuration(), CvRenewalOrderMapper.class.getName()),
            CvRenewalOrder.class);
        when(customerMapper.selectById(eq(1001L))).thenReturn(activeCustomer());
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"));
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
        stubLicenseIssuesByLicenseId(revoked);

        assertThrows(ServiceException.class, () -> service.insertRenewalOrder(validOrderBo()));

        verify(renewalOrderMapper, never()).insert(any(CvRenewalOrder.class));
    }

    @Test
    void mapsCurrencyWhenCreatingRenewalMetadata() {
        service.insertRenewalOrder(validOrderBo());

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).insert(orderCaptor.capture());
        assertEquals("CNY", orderCaptor.getValue().getCurrency());
    }

    @Test
    void appliesPaymentCallbackAsVendorOrderAuthorizationMetadata() {
        stubRenewalOrdersByOrderNo(existingOrder());
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));
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
        assertEquals("issued", update.getIssueStatus());
        assertEquals("mock-pay", update.getPayChannel());
        assertEquals("LIC-RENEWED-001", update.getIssuedLicenseId());
        assertEquals(Date.from(Instant.parse("2026-06-20T00:00:00Z")), update.getPaidTime());
    }

    @Test
    void automaticAuthorizedCallbackWithoutIssuedLicenseEntersIssuing() {
        stubRenewalOrdersByOrderNo(existingOrder());
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).update(orderCaptor.capture(), any());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("authorized", update.getOrderStatus());
        assertEquals("issuing", update.getIssueStatus());
        assertNull(update.getIssuedLicenseId());
    }

    @Test
    void automaticAuthorizedCallbackWithoutIssuedLicenseDoesNotRetainStaleIssuedLicense() {
        CvRenewalOrder order = existingOrder();
        order.setIssuedLicenseId("LIC-STALE-001");
        stubRenewalOrdersByOrderNo(order);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        ArgumentCaptor<LambdaUpdateWrapper<CvRenewalOrder>> wrapperCaptor =
            ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(renewalOrderMapper).update(orderCaptor.capture(), wrapperCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("authorized", update.getOrderStatus());
        assertEquals("issuing", update.getIssueStatus());
        assertNull(update.getIssuedLicenseId());
        LambdaUpdateWrapper<CvRenewalOrder> wrapper = wrapperCaptor.getValue();
        assertTrue(wrapper.getSqlSet().contains("issuedLicenseId"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue(null));
    }

    @Test
    void manualAuthorizedCallbackWithoutIssuedLicenseStaysPendingIssue() {
        CvRenewalOrder order = existingOrder();
        order.setPayChannel("manual");
        stubRenewalOrdersByOrderNo(order);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");
        callback.setPayChannel("manual");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("authorized", update.getOrderStatus());
        assertEquals("pending_issue", update.getIssueStatus());
        assertNull(update.getIssuedLicenseId());
    }

    @Test
    void openApiAuthorizedCallbackWithoutIssuedLicenseStaysPendingIssue() {
        CvRenewalOrder order = existingOrder();
        order.setRequestSource("open-api");
        stubRenewalOrdersByOrderNo(order);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("authorized", update.getOrderStatus());
        assertEquals("pending_issue", update.getIssueStatus());
        assertNull(update.getIssuedLicenseId());
    }

    @Test
    void marksPaidCallbackWithoutIssuedLicenseAsPendingIssue() {
        stubRenewalOrdersByOrderNo(existingOrder());

        service.applyRenewalCallback(validPaidCallback());

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("paid", update.getOrderStatus());
        assertEquals("pending_issue", update.getIssueStatus());
    }

    @Test
    void bindsValidatedIssuedLicenseFromManualPaidCallback() {
        stubRenewalOrdersByOrderNo(existingOrder());
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("paid", update.getOrderStatus());
        assertEquals("issued", update.getIssueStatus());
        assertEquals("LIC-RENEWED-001", update.getIssuedLicenseId());
    }

    @Test
    void rejectsManualBindingWhenIssuedLicenseBelongsToDifferentCustomer() {
        CvLicenseIssue mismatchedIssuedLicense = activeLicense("LIC-RENEWED-OTHER");
        mismatchedIssuedLicense.setCustomerId(2002L);
        stubRenewalOrdersByOrderNo(existingOrder());
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), mismatchedIssuedLicense);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-OTHER");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license metadata does not match vendor customer", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicenseDoesNotExist() {
        stubRenewalOrdersByOrderNo(existingOrder());
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"));
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-MISSING-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license metadata does not exist", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicenseInstallIdDiffers() {
        CvRenewalOrder order = existingOrder();
        order.setInstallId("INSTALL-001");
        CvLicenseIssue renewed = activeLicense("LIC-RENEWED-001");
        renewed.setInstallId("INSTALL-OTHER");
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), renewed);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license installId does not match renewal order", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicensePackageDiffers() {
        CvRenewalOrder order = existingOrder();
        order.setRequestedPackageId(3001L);
        CvLicenseIssue renewed = activeLicense("LIC-RENEWED-001");
        renewed.setPackageId(3002L);
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), renewed);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license package does not match renewal order", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicenseEditionDiffers() {
        CvRenewalOrder order = existingOrder();
        order.setRequestedEdition("PRO");
        CvLicenseIssue renewed = activeLicense("LIC-RENEWED-001");
        renewed.setEdition("BASIC");
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), renewed);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license edition does not match renewal order", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicensePackageNameDiffers() {
        CvRenewalOrder order = existingOrder();
        order.setRequestedPackageName("专业版");
        CvLicenseIssue renewed = activeLicense("LIC-RENEWED-001");
        renewed.setPackageName("标准版");
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), renewed);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license package name does not match renewal order", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicenseValidityOverlapsExistingCustomerLicense() {
        CvRenewalOrder order = existingOrder();
        order.setInstallId("INSTALL-001");
        order.setRequestedPackageId(1001L);
        order.setRequestedPackageName("标准版");
        order.setRequestedEdition("标准版");
        CvLicenseIssue original = activeLicense("LIC-ORIGINAL-001");
        original.setValidFrom(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        original.setValidTo(Date.from(Instant.parse("2026-07-01T00:00:00Z")));
        CvLicenseIssue renewed = activeLicense("LIC-RENEWED-001");
        renewed.setInstallId("INSTALL-001");
        renewed.setPackageId(1001L);
        renewed.setPackageName("标准版");
        renewed.setEdition("标准版");
        renewed.setValidFrom(Date.from(Instant.parse("2026-06-01T00:00:00Z")));
        renewed.setValidTo(Date.from(Instant.parse("2027-06-01T00:00:00Z")));
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(original, renewed);
        when(licenseIssueMapper.selectOne(any(), eq(false))).thenReturn(original);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license validity overlaps existing customer license", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsManualBindingWhenIssuedLicenseIsRevoked() {
        CvLicenseIssue renewed = activeLicense("LIC-RENEWED-001");
        renewed.setIssueStatus("revoked");
        renewed.setRevokedTime(Date.from(Instant.parse("2026-06-21T00:00:00Z")));
        stubRenewalOrdersByOrderNo(existingOrder());
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), renewed);
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(callback));

        assertEquals("Issued renewal license is revoked", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
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
        stubRenewalOrdersByOrderNo(existingOrder());
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
        stubRenewalOrdersByOrderNo(order);
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
        stubRenewalOrdersByOrderNo(order);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(validPaidCallback()));

        assertEquals("Renewal callback cannot overwrite terminal order status", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void allowsPaidOrderToBecomeAuthorizedWhenIssuedLicenseArrives() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("paid");
        order.setIssueStatus("pending_issue");
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");
        callback.setIssuedLicenseId("LIC-RENEWED-001");

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("authorized", update.getOrderStatus());
        assertEquals("issued", update.getIssueStatus());
        assertEquals("LIC-RENEWED-001", update.getIssuedLicenseId());
    }

    @Test
    void preservesExistingPaidTimeWhenAuthorizedCallbackOmitsPaidTime() {
        Date existingPaidTime = Date.from(Instant.parse("2026-06-19T00:00:00Z"));
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("paid");
        order.setIssueStatus("pending_issue");
        order.setPaidTime(existingPaidTime);
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");
        callback.setIssuedLicenseId("LIC-RENEWED-001");
        callback.setPaidTime(null);

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals(existingPaidTime, update.getPaidTime());
    }

    @Test
    void keepsPaidTimeNullWhenAuthorizedCallbackOmitsPaidTimeAndExistingIsNull() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("paid");
        order.setIssueStatus("pending_issue");
        order.setPaidTime(null);
        stubRenewalOrdersByOrderNo(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));
        CvRenewalCallbackRequest callback = validPaidCallback();
        callback.setOrderStatus("authorized");
        callback.setIssuedLicenseId("LIC-RENEWED-001");
        callback.setPaidTime(null);

        service.applyRenewalCallback(callback);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertNull(update.getPaidTime());
    }

    @Test
    void retriesFailedIssueBackToIssuingWhenNoIssuedLicenseExists() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("authorized");
        order.setIssueStatus("issue_failed");
        when(renewalOrderMapper.selectById(eq(501L))).thenReturn(order);

        service.retryRenewalIssue(501L);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals(501L, update.getId());
        assertEquals("issuing", update.getIssueStatus());
    }

    @Test
    void retriesFailedIssueToIssuedWhenIssuedLicenseExists() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("authorized");
        order.setIssueStatus("issue_failed");
        order.setIssuedLicenseId("LIC-RENEWED-001");
        when(renewalOrderMapper.selectById(eq(501L))).thenReturn(order);
        stubLicenseIssuesByLicenseId(activeLicense("LIC-ORIGINAL-001"), activeLicense("LIC-RENEWED-001"));

        service.retryRenewalIssue(501L);

        ArgumentCaptor<CvRenewalOrder> orderCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper).updateById(orderCaptor.capture());
        CvRenewalOrder update = orderCaptor.getValue();
        assertEquals("issued", update.getIssueStatus());
    }

    @Test
    void rejectsRetryForManualFailedIssueStatus() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("paid");
        order.setIssueStatus("issue_failed");
        when(renewalOrderMapper.selectById(eq(501L))).thenReturn(order);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.retryRenewalIssue(501L));

        assertEquals("Only automatic failed renewal issue can be retried", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsRetryForManualAuthorizedFailedIssueStatus() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("authorized");
        order.setIssueStatus("issue_failed");
        order.setPayChannel("manual");
        when(renewalOrderMapper.selectById(eq(501L))).thenReturn(order);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.retryRenewalIssue(501L));

        assertEquals("Only automatic failed renewal issue can be retried", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsRetryForOpenApiAuthorizedFailedIssueStatus() {
        CvRenewalOrder order = existingOrder();
        order.setOrderStatus("authorized");
        order.setIssueStatus("issue_failed");
        order.setRequestSource("open-api");
        when(renewalOrderMapper.selectById(eq(501L))).thenReturn(order);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.retryRenewalIssue(501L));

        assertEquals("Only automatic failed renewal issue can be retried", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsRetryForNonFailedIssueStatus() {
        CvRenewalOrder order = existingOrder();
        order.setIssueStatus("pending_issue");
        when(renewalOrderMapper.selectById(eq(501L))).thenReturn(order);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.retryRenewalIssue(501L));

        assertEquals("Only failed renewal issue can be retried", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
    }

    @Test
    void rejectsCallbackWhenOrderNoMatchesDuplicateOrders() {
        CvRenewalOrder first = existingOrder();
        CvRenewalOrder second = existingOrder();
        second.setId(502L);
        stubRenewalOrdersByOrderNo(first, second);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(validPaidCallback()));

        assertEquals("Duplicate renewal order metadata", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
        verify(renewalOrderMapper, never()).update(any(CvRenewalOrder.class), any());
    }

    @Test
    void rejectsCallbackWhenLicenseIdMatchesDuplicateIssues() {
        stubRenewalOrdersByOrderNo(existingOrder());
        CvLicenseIssue first = activeLicense("LIC-ORIGINAL-001");
        CvLicenseIssue second = activeLicense("LIC-ORIGINAL-001");
        stubLicenseIssuesByLicenseId(first, second);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.applyRenewalCallback(validPaidCallback()));

        assertEquals("Duplicate license issue metadata", exception.getMessage());
        verify(renewalOrderMapper, never()).updateById(any(CvRenewalOrder.class));
        verify(renewalOrderMapper, never()).update(any(CvRenewalOrder.class), any());
    }

    private CvRenewalOrderBo validOrderBo() {
        CvRenewalOrderBo bo = new CvRenewalOrderBo();
        bo.setOrderNo("REN-202606-001");
        bo.setCustomerId(1001L);
        bo.setLicenseId("LIC-ORIGINAL-001");
        bo.setAmount(new BigDecimal("12800.00"));
        bo.setCurrency("CNY");
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

    private void stubRenewalOrdersByOrderNo(CvRenewalOrder... orders) {
        when(renewalOrderMapper.selectList(any())).thenAnswer(invocation -> List.of(orders));
        when(renewalOrderMapper.selectList(any(IPage.class), any(Wrapper.class))).thenAnswer(invocation -> List.of(orders));
    }

    private void stubLicenseIssuesByLicenseId(CvLicenseIssue... issues) {
        Map<String, List<CvLicenseIssue>> issuesByLicenseId = new HashMap<>();
        for (CvLicenseIssue issue : issues) {
            issuesByLicenseId.compute(issue.getLicenseId(), (licenseId, existingIssues) -> {
                if (existingIssues == null) {
                    return List.of(issue);
                }
                java.util.ArrayList<CvLicenseIssue> combinedIssues = new java.util.ArrayList<>(existingIssues);
                combinedIssues.add(issue);
                return combinedIssues;
            });
        }
        when(licenseIssueMapper.selectList(any())).thenAnswer(invocation -> matchingIssues(invocation.getArgument(0), issuesByLicenseId));
        when(licenseIssueMapper.selectList(any(IPage.class), any(Wrapper.class))).thenAnswer(invocation -> matchingIssues(invocation.getArgument(1), issuesByLicenseId));
    }

    private List<CvLicenseIssue> matchingIssues(Object wrapper, Map<String, List<CvLicenseIssue>> issuesByLicenseId) {
            if (wrapper instanceof AbstractWrapper<?, ?, ?> queryWrapper) {
                queryWrapper.getSqlSegment();
                for (Object value : queryWrapper.getParamNameValuePairs().values()) {
                    List<CvLicenseIssue> matchingIssues = issuesByLicenseId.get(value);
                    if (matchingIssues != null) {
                        return matchingIssues;
                    }
                }
            }
            return List.of();
    }

    private CvRenewalOrder existingOrder() {
        CvRenewalOrder order = new CvRenewalOrder();
        order.setId(501L);
        order.setOrderNo("REN-202606-001");
        order.setCustomerId(1001L);
        order.setLicenseId("LIC-ORIGINAL-001");
        order.setPayChannel("mock-pay");
        order.setOrderStatus("pending");
        order.setIssueStatus("pending_issue");
        return order;
    }
}
