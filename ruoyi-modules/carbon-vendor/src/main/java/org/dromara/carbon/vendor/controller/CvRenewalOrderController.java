package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvRenewalOrderBo;
import org.dromara.carbon.vendor.domain.renewal.CvRenewalCallbackRequest;
import org.dromara.carbon.vendor.domain.vo.CvRenewalOrderVo;
import org.dromara.carbon.vendor.service.ICvRenewalOrderService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor renewal order API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/renewal-order")
public class CvRenewalOrderController extends BaseController {

    private final ICvRenewalOrderService renewalOrderService;

    /**
     * List vendor renewal orders.
     */
    @SaCheckPermission("vendor:renewalOrder:list")
    @GetMapping("/list")
    public TableDataInfo<CvRenewalOrderVo> list(CvRenewalOrderBo renewalOrder, PageQuery pageQuery) {
        return renewalOrderService.selectPageRenewalOrderList(renewalOrder, pageQuery);
    }

    /**
     * Get vendor renewal order details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:renewalOrder:query")
    @GetMapping("/{id}")
    public R<CvRenewalOrderVo> getInfo(@PathVariable Long id) {
        return R.ok(renewalOrderService.selectRenewalOrderById(id));
    }

    /**
     * Add vendor renewal order.
     */
    @SaCheckPermission("vendor:renewalOrder:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvRenewalOrderBo renewalOrder) {
        return toAjax(renewalOrderService.insertRenewalOrder(renewalOrder));
    }

    /**
     * Edit vendor renewal order.
     */
    @SaCheckPermission("vendor:renewalOrder:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvRenewalOrderBo renewalOrder) {
        return toAjax(renewalOrderService.updateRenewalOrder(renewalOrder));
    }

    /**
     * Apply vendor payment/authorization callback metadata.
     */
    @SaCheckPermission("vendor:renewalOrder:edit")
    @PostMapping("/callback")
    public R<Void> callback(@RequestBody CvRenewalCallbackRequest callbackRequest) {
        return toAjax(renewalOrderService.applyRenewalCallback(callbackRequest));
    }

    /**
     * Delete vendor renewal orders.
     *
     * @param ids primary keys
     */
    @SaCheckPermission("vendor:renewalOrder:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(renewalOrderService.deleteRenewalOrderByIds(ids));
    }
}
