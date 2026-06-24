package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvCustomerBo;
import org.dromara.carbon.vendor.domain.vo.CvCustomerVo;
import org.dromara.carbon.vendor.service.ICvCustomerService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
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

import java.util.List;

/**
 * Vendor customer archive controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/customer")
public class CvCustomerController extends BaseController {

    private final ICvCustomerService customerService;

    @SaCheckPermission("vendor:customer:list")
    @GetMapping("/list")
    public TableDataInfo<CvCustomerVo> list(CvCustomerBo bo, PageQuery pageQuery) {
        return customerService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("vendor:customer:export")
    @Log(title = "客户档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CvCustomerBo bo, HttpServletResponse response) {
        List<CvCustomerVo> list = customerService.queryList(bo);
        ExcelUtil.exportExcel(list, "客户档案", CvCustomerVo.class, response);
    }

    @SaCheckPermission("vendor:customer:query")
    @GetMapping("/{id}")
    public R<CvCustomerVo> getInfo(@NotNull(message = "客户主键不能为空") @PathVariable Long id) {
        return R.ok(customerService.queryById(id));
    }

    @SaCheckPermission("vendor:customer:add")
    @Log(title = "客户档案", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvCustomerBo bo) {
        if (!customerService.checkCustomerCodeUnique(bo)) {
            return R.fail("新增客户档案'" + bo.getCustomerCode() + "'失败，客户编码已存在");
        }
        return toAjax(customerService.insertByBo(bo));
    }

    @SaCheckPermission("vendor:customer:edit")
    @Log(title = "客户档案", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvCustomerBo bo) {
        if (!customerService.checkCustomerCodeUnique(bo)) {
            return R.fail("修改客户档案'" + bo.getCustomerCode() + "'失败，客户编码已存在");
        }
        return toAjax(customerService.updateByBo(bo));
    }

    @SaCheckPermission("vendor:customer:remove")
    @Log(title = "客户档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "客户主键不能为空") @PathVariable Long[] ids) {
        return toAjax(customerService.deleteWithValidByIds(List.of(ids), true));
    }
}
