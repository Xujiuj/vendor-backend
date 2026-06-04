package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvCustomerBo;
import org.dromara.carbon.vendor.domain.vo.CvCustomerVo;
import org.dromara.carbon.vendor.service.ICvCustomerService;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor customer read API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/customer")
public class CvCustomerController extends BaseController {

    private final ICvCustomerService customerService;

    /**
     * List vendor customers.
     */
    @SaCheckPermission("vendor:customer:list")
    @GetMapping("/list")
    public TableDataInfo<CvCustomerVo> list(CvCustomerBo customer, PageQuery pageQuery) {
        return customerService.selectPageCustomerList(customer, pageQuery);
    }

    /**
     * Get vendor customer details.
     *
     * @param id customer id
     */
    @SaCheckPermission("vendor:customer:query")
    @GetMapping("/{id}")
    public R<CvCustomerVo> getInfo(@PathVariable Long id) {
        return R.ok(customerService.selectCustomerById(id));
    }
}
