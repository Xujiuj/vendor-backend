package org.dromara.system.controller.open;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.system.domain.vo.TenantPackagePurchaseVo;
import org.dromara.system.service.ISysTenantPackageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public tenant package purchase preparation API.
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/tenant-packages")
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class TenantPackageOpenController {

    private final ISysTenantPackageService tenantPackageService;

    /**
     * 查询可在线购买的套餐列表。
     */
    @GetMapping
    public R<List<TenantPackagePurchaseVo>> listPurchasablePackages() {
        return R.ok(tenantPackageService.selectPurchasableList());
    }

}
