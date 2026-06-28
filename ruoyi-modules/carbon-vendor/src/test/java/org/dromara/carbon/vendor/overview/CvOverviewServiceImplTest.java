package org.dromara.carbon.vendor.overview;

import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.customer.mapper.CvCustomerMapper;
import org.dromara.carbon.vendor.license.domain.CvLicenseIssue;
import org.dromara.carbon.vendor.license.mapper.CvLicenseIssueMapper;
import org.dromara.carbon.vendor.overview.domain.vo.CvOverviewVo;
import org.dromara.carbon.vendor.overview.service.impl.CvOverviewServiceImpl;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.renewal.mapper.CvRenewalOrderMapper;
import org.dromara.carbon.vendor.template.domain.CvReportTemplateDownloadToken;
import org.dromara.carbon.vendor.template.mapper.CvReportTemplateDownloadTokenMapper;
import org.dromara.system.domain.SysTenantPackage;
import org.dromara.system.mapper.SysTenantPackageMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("dev")
class CvOverviewServiceImplTest {

    @Test
    void overviewUsesCurrentPackageNamesAndDeduplicatesOperationalItems() {
        CvCustomerMapper customerMapper = mock(CvCustomerMapper.class);
        CvLicenseIssueMapper licenseIssueMapper = mock(CvLicenseIssueMapper.class);
        CvReportTemplateDownloadTokenMapper downloadTokenMapper = mock(CvReportTemplateDownloadTokenMapper.class);
        CvRenewalOrderMapper renewalOrderMapper = mock(CvRenewalOrderMapper.class);
        SysTenantPackageMapper tenantPackageMapper = mock(SysTenantPackageMapper.class);
        CvOverviewServiceImpl service = new CvOverviewServiceImpl(
            customerMapper,
            licenseIssueMapper,
            downloadTokenMapper,
            renewalOrderMapper,
            tenantPackageMapper
        );

        when(customerMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.selectList(any())).thenReturn(List.of(customer(1001L)));
        when(licenseIssueMapper.selectCount(any())).thenReturn(0L);
        when(licenseIssueMapper.selectList(any())).thenReturn(
            List.of(license("LIC-CHART-1", 1001L, "Enterprise Plan")),
            List.of(license("LIC-EXP-1", 1001L, "Enterprise Plan"), license("LIC-EXP-2", 1001L, "Enterprise Plan")),
            List.of(license("LIC-EXP-1", 1001L, "Enterprise Plan"), license("LIC-EXP-2", 1001L, "Enterprise Plan"))
        );
        when(tenantPackageMapper.selectBatchIds(any())).thenReturn(List.of(packageConfig()));
        when(renewalOrderMapper.selectList(any())).thenReturn(List.of(pendingRenewalOrder()));
        when(downloadTokenMapper.selectCount(any())).thenReturn(0L);
        when(downloadTokenMapper.selectList(any())).thenReturn(
            List.of(downloadToken("TOKEN-1"), downloadToken("TOKEN-2")),
            List.of(downloadToken("TOKEN-1"), downloadToken("TOKEN-2"))
        );

        CvOverviewVo overview = service.queryOverview();

        assertEquals(List.of("Current Plan"), overview.getAuthorizationChart().getSeries().stream()
            .map(CvOverviewVo.Series::getName)
            .toList());
        assertEquals(2, overview.getReminders().size());
        assertEquals(2, overview.getTodos().size());
        assertTrue(overview.getTodos().stream().anyMatch(todo -> todo.getDescription().contains("续费订单")));
        assertTrue(overview.getTodos().stream().anyMatch(todo -> todo.getDescription().contains("已分发待确认")));
        assertFalse(overview.getTodos().stream().anyMatch(todo -> todo.getDescription().contains("天后到期")));
    }

    private CvCustomer customer(Long id) {
        CvCustomer customer = new CvCustomer();
        customer.setId(id);
        customer.setCustomerCode("CUST-001");
        customer.setCustomerName("Customer A");
        customer.setCustomerStatus("active");
        return customer;
    }

    private CvLicenseIssue license(String licenseId, Long customerId, String packageName) {
        CvLicenseIssue issue = new CvLicenseIssue();
        issue.setLicenseId(licenseId);
        issue.setCustomerId(customerId);
        issue.setPackageId(1001L);
        issue.setPackageName(packageName);
        issue.setIssueStatus("issued");
        issue.setIssuedTime(Date.from(Instant.now()));
        issue.setValidFrom(Date.from(Instant.now().minusSeconds(86400)));
        issue.setValidTo(Date.from(Instant.now().plusSeconds(86400 * 7)));
        return issue;
    }

    private SysTenantPackage packageConfig() {
        SysTenantPackage tenantPackage = new SysTenantPackage();
        tenantPackage.setPackageId(1001L);
        tenantPackage.setPackageName("Current Plan");
        tenantPackage.setStatus("0");
        tenantPackage.setDelFlag("0");
        return tenantPackage;
    }

    private CvRenewalOrder pendingRenewalOrder() {
        CvRenewalOrder order = new CvRenewalOrder();
        order.setId(2001L);
        order.setOrderNo("RO-001");
        order.setCustomerId(1001L);
        order.setOrderStatus("pending");
        order.setCreateTime(Date.from(Instant.now()));
        return order;
    }

    private CvReportTemplateDownloadToken downloadToken(String tokenId) {
        CvReportTemplateDownloadToken token = new CvReportTemplateDownloadToken();
        token.setDownloadToken(tokenId);
        token.setCustomerId(1001L);
        token.setLicenseId("LIC-EXP-1");
        token.setTemplateId(3001L);
        token.setFileName("monthly-template.xlsx");
        token.setTokenStatus("issued");
        token.setCreateTime(Date.from(Instant.now()));
        return token;
    }
}
