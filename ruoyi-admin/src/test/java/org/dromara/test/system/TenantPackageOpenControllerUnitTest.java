package org.dromara.test.system;

import org.dromara.common.core.domain.R;
import org.dromara.system.controller.open.TenantPackageOpenController;
import org.dromara.system.domain.vo.TenantPackagePurchaseVo;
import org.dromara.system.service.ISysTenantPackageService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class TenantPackageOpenControllerUnitTest {

    @Test
    void listPurchasablePackagesReturnsServiceContract() {
        ISysTenantPackageService tenantPackageService = mock(ISysTenantPackageService.class);
        TenantPackagePurchaseVo tenantPackage = new TenantPackagePurchaseVo();
        tenantPackage.setPackageId(1001L);
        tenantPackage.setPackageName("标准版");
        tenantPackage.setRemark("默认业务套餐");
        tenantPackage.setPriceAmount(new BigDecimal("1999.00"));
        tenantPackage.setPriceCurrency("CNY");
        tenantPackage.setBillingCycle("YEAR");
        when(tenantPackageService.selectPurchasableList()).thenReturn(List.of(tenantPackage));

        TenantPackageOpenController controller = new TenantPackageOpenController(tenantPackageService);
        R<List<TenantPackagePurchaseVo>> response = controller.listPurchasablePackages();

        assertEquals(200, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("标准版", response.getData().get(0).getPackageName());
        assertEquals(new BigDecimal("1999.00"), response.getData().get(0).getPriceAmount());
        verify(tenantPackageService).selectPurchasableList();
    }
}
