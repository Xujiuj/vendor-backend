package org.dromara.carbon.vendor.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.dromara.carbon.vendor.config.VendorPaymentProperties;
import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.bo.PaymentNotifyBo;
import org.dromara.carbon.vendor.domain.license.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.domain.license.CvLicenseIssueResult;
import org.dromara.carbon.vendor.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.service.ICvLicenseIssueService;
import org.dromara.carbon.vendor.service.impl.OnlinePurchaseServiceImpl;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.domain.vo.SysTenantPackageVo;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class OnlinePurchaseServiceImplTest {

    private CvRenewalOrderMapper renewalOrderMapper;
    private SysTenantPackageMapper tenantPackageMapper;
    private ICvLicenseIssueService licenseIssueService;
    private OnlinePurchaseServiceImpl service;

    @BeforeEach
    void setUp() {
        renewalOrderMapper = mock(CvRenewalOrderMapper.class);
        CvCustomerMapper customerMapper = mock(CvCustomerMapper.class);
        tenantPackageMapper = mock(SysTenantPackageMapper.class);
        licenseIssueService = mock(ICvLicenseIssueService.class);
        VendorPaymentProperties paymentProperties = new VendorPaymentProperties();
        paymentProperties.setRequireSignature(false);
        service = new OnlinePurchaseServiceImpl(
            renewalOrderMapper,
            customerMapper,
            tenantPackageMapper,
            paymentProperties,
            licenseIssueService,
            new ObjectMapper()
        );

        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new Configuration(), CvRenewalOrderMapper.class.getName()),
            CvRenewalOrder.class);
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new Configuration(), CvLicenseIssueMapper.class.getName()),
            CvLicenseIssue.class);
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new Configuration(), SysTenantPackageMapper.class.getName()),
            SysTenantPackage.class);
    }

    @Test
    void paidOnlineOrderIssuesLicenseFromPackageConfiguration() {
        CvRenewalOrder order = pendingOrder();
        SysTenantPackageVo tenantPackage = autoIssuePackage();
        CvLicenseIssue issue = new CvLicenseIssue();
        issue.setLicenseId("LIC-AUTO-001");
        when(renewalOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(tenantPackageMapper.selectVoById(1001L)).thenReturn(tenantPackage);
        when(licenseIssueService.issueManualLicense(any(CvLicenseIssueRequest.class)))
            .thenReturn(CvLicenseIssueResult.issued("{}", issue));

        service.markPaid(paidNotify());

        ArgumentCaptor<CvLicenseIssueRequest> requestCaptor = ArgumentCaptor.forClass(CvLicenseIssueRequest.class);
        verify(licenseIssueService).issueManualLicense(requestCaptor.capture());
        CvLicenseIssueRequest request = requestCaptor.getValue();
        assertEquals(10L, request.getCustomerId());
        assertEquals(1001L, request.getPackageId());
        assertEquals("KEY-2026", request.getKeyId());
        assertEquals("INSTALL-AUTO-001", request.getInstallId());
        assertEquals("feature-a", request.getFeatures().get(0));
        assertEquals("RPT-001", request.getTemplateEntitlements().get(0).getTemplateCode());
        assertNotNull(request.getValidFrom());
        assertNotNull(request.getValidTo());

        ArgumentCaptor<CvRenewalOrder> updateCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper, org.mockito.Mockito.times(2)).updateById(updateCaptor.capture());
        CvRenewalOrder issueUpdate = updateCaptor.getAllValues().get(1);
        assertEquals("issued", issueUpdate.getIssueStatus());
        assertEquals("LIC-AUTO-001", issueUpdate.getIssuedLicenseId());
    }

    @Test
    void paidOnlineOrderMarksIssueFailedWhenPackageLicenseConfigIsIncomplete() {
        CvRenewalOrder order = pendingOrder();
        SysTenantPackageVo tenantPackage = autoIssuePackage();
        tenantPackage.setLicenseKeyId(null);
        when(renewalOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);
        when(tenantPackageMapper.selectVoById(1001L)).thenReturn(tenantPackage);

        service.markPaid(paidNotify());

        ArgumentCaptor<CvRenewalOrder> updateCaptor = ArgumentCaptor.forClass(CvRenewalOrder.class);
        verify(renewalOrderMapper, org.mockito.Mockito.times(2)).updateById(updateCaptor.capture());
        CvRenewalOrder issueUpdate = updateCaptor.getAllValues().get(1);
        assertEquals("issue_failed", issueUpdate.getIssueStatus());
    }

    private CvRenewalOrder pendingOrder() {
        CvRenewalOrder order = new CvRenewalOrder();
        order.setId(501L);
        order.setOrderNo("PO-AUTO-001");
        order.setCustomerId(10L);
        order.setInstallId("INSTALL-AUTO-001");
        order.setRequestedPackageId(1001L);
        order.setRequestedPackageName("标准版");
        order.setRequestedEdition("标准版");
        order.setOrderStatus("pending");
        order.setIssueStatus("pending_issue");
        order.setPayChannel("ALIPAY");
        order.setAmount(new BigDecimal("1.00"));
        order.setCurrency("CNY");
        order.setPaidTime(new Date());
        return order;
    }

    private PaymentNotifyBo paidNotify() {
        PaymentNotifyBo bo = new PaymentNotifyBo();
        bo.setOrderNo("PO-AUTO-001");
        bo.setPayChannel("ALIPAY");
        bo.setTradeNo("ALI-AUTO-001");
        bo.setTradeStatus("TRADE_SUCCESS");
        bo.setAmount(new BigDecimal("1.00"));
        bo.setCurrency("CNY");
        return bo;
    }

    private SysTenantPackageVo autoIssuePackage() {
        SysTenantPackageVo vo = new SysTenantPackageVo();
        vo.setPackageId(1001L);
        vo.setPackageName("标准版");
        vo.setBillingCycle("YEAR");
        vo.setLicenseAutoIssueEnabled(true);
        vo.setLicenseKeyId("KEY-2026");
        vo.setLicenseValidityDays(365);
        vo.setLicenseFeatureCodes("feature-a,feature-b");
        vo.setLicenseTemplateEntitlements("[{\"templateCode\":\"RPT-001\",\"templateVersion\":\"v1\",\"scope\":\"all\"}]");
        return vo;
    }
}
