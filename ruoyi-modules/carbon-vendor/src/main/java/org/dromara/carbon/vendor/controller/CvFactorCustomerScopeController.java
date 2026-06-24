package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.carbon.vendor.service.ICvFactorCustomerScopeService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
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
 * Vendor factor customer scope API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/factor-customer-scope")
public class CvFactorCustomerScopeController extends BaseController {

    private final ICvFactorCustomerScopeService factorCustomerScopeService;

    /**
     * List vendor factor customer scopes.
     */
    @SaCheckPermission("vendor:factorCustomerScope:list")
    @GetMapping("/list")
    public TableDataInfo<CvFactorCustomerScopeVo> list(CvFactorCustomerScopeBo factorCustomerScope, PageQuery pageQuery) {
        return factorCustomerScopeService.selectPageFactorCustomerScopeList(factorCustomerScope, pageQuery);
    }

    /**
     * Get vendor factor customer scope details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:factorCustomerScope:query")
    @GetMapping("/{id}")
    public R<CvFactorCustomerScopeVo> getInfo(@PathVariable Long id) {
        return R.ok(factorCustomerScopeService.selectFactorCustomerScopeById(id));
    }

    /**
     * Add vendor factor customer scope.
     */
    @Log(title = "因子客户范围", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorCustomerScope:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvFactorCustomerScopeBo factorCustomerScope) {
        return toAjax(factorCustomerScopeService.insertFactorCustomerScope(factorCustomerScope));
    }

    /**
     * Edit vendor factor customer scope.
     */
    @Log(title = "因子客户范围", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorCustomerScope:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvFactorCustomerScopeBo factorCustomerScope) {
        return toAjax(factorCustomerScopeService.updateFactorCustomerScope(factorCustomerScope));
    }

    /**
     * Delete vendor factor customer scopes.
     *
     * @param ids primary keys
     */
    @Log(title = "因子客户范围", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:factorCustomerScope:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(factorCustomerScopeService.deleteFactorCustomerScopeByIds(ids));
    }
}
