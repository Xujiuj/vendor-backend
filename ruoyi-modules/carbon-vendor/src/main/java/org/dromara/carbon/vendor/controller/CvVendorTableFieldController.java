package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.domain.vo.CvVendorTableFieldVo;
import org.dromara.carbon.vendor.service.ICvVendorTableFieldService;
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
 * Vendor table field definition API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/table-field")
public class CvVendorTableFieldController extends BaseController {

    private final ICvVendorTableFieldService tableFieldService;

    @SaCheckPermission(value = {
        "vendor:factorRecord:list",
        "vendor:dimension:list"
    }, mode = SaMode.OR)
    @GetMapping("/list")
    public TableDataInfo<CvVendorTableFieldVo> list(CvVendorTableFieldBo bo, PageQuery pageQuery) {
        return tableFieldService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission(value = {
        "vendor:factorRecord:query",
        "vendor:dimension:query"
    }, mode = SaMode.OR)
    @GetMapping("/{id}")
    public R<CvVendorTableFieldVo> getInfo(@NotNull(message = "字段定义ID不能为空") @PathVariable Long id) {
        return R.ok(tableFieldService.queryById(id));
    }

    @Log(title = "表字段定义", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission(value = {
        "vendor:factorRecord:add",
        "vendor:dimension:add"
    }, mode = SaMode.OR)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvVendorTableFieldBo bo) {
        return toAjax(tableFieldService.insertByBo(bo));
    }

    @Log(title = "表字段定义", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission(value = {
        "vendor:factorRecord:edit",
        "vendor:dimension:edit"
    }, mode = SaMode.OR)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvVendorTableFieldBo bo) {
        return toAjax(tableFieldService.updateByBo(bo));
    }

    @Log(title = "表字段定义", businessType = BusinessType.DELETE)
    @SaCheckPermission(value = {
        "vendor:factorRecord:remove",
        "vendor:dimension:remove"
    }, mode = SaMode.OR)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "字段定义ID不能为空") @PathVariable Long[] ids) {
        return toAjax(tableFieldService.deleteByIds(ids));
    }
}
