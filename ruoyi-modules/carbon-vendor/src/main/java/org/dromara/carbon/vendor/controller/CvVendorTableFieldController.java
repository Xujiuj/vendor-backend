package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.CvVendorTableField;
import org.dromara.carbon.vendor.domain.bo.CvVendorTableFieldBo;
import org.dromara.carbon.vendor.service.ICvVendorTableFieldService;
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

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/table-field")
public class CvVendorTableFieldController extends BaseController {

    private final ICvVendorTableFieldService tableFieldService;

    @SaCheckPermission(value = {"vendor:dimension:list", "vendor:factorRecord:list"}, mode = SaMode.OR)
    @GetMapping("/list")
    public TableDataInfo<CvVendorTableField> list(CvVendorTableFieldBo bo, PageQuery pageQuery) {
        checkPermission(bo == null ? null : bo.getTableGroup(), "list");
        return tableFieldService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission(value = {"vendor:dimension:query", "vendor:factorRecord:query"}, mode = SaMode.OR)
    @GetMapping("/{id}")
    public R<CvVendorTableField> getInfo(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        CvVendorTableField field = tableFieldService.queryById(id);
        checkPermission(field == null ? null : field.getTableGroup(), "query");
        return R.ok(field);
    }

    @SaCheckPermission(value = {"vendor:dimension:add", "vendor:factorRecord:add"}, mode = SaMode.OR)
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvVendorTableFieldBo bo) {
        checkPermission(bo.getTableGroup(), "add");
        return toAjax(tableFieldService.insertByBo(bo));
    }

    @SaCheckPermission(value = {"vendor:dimension:edit", "vendor:factorRecord:edit"}, mode = SaMode.OR)
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvVendorTableFieldBo bo) {
        checkPermission(bo.getTableGroup(), "edit");
        return toAjax(tableFieldService.updateByBo(bo));
    }

    @SaCheckPermission(value = {"vendor:dimension:remove", "vendor:factorRecord:remove"}, mode = SaMode.OR)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "ids cannot be empty") @PathVariable Long[] ids) {
        for (Long id : ids) {
            CvVendorTableField field = tableFieldService.queryById(id);
            checkPermission(field == null ? null : field.getTableGroup(), "remove");
        }
        return toAjax(tableFieldService.deleteByIds(List.of(ids)));
    }

    private void checkPermission(String tableGroup, String action) {
        if ("factor".equals(tableGroup)) {
            StpUtil.checkPermission("vendor:factorRecord:" + action);
            return;
        }
        StpUtil.checkPermission("vendor:dimension:" + action);
    }
}
