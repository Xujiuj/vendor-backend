package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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

    @SaCheckPermission("vendor:dimension:list")
    @GetMapping("/list")
    public TableDataInfo<CvVendorTableField> list(CvVendorTableFieldBo bo, PageQuery pageQuery) {
        return tableFieldService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("vendor:dimension:query")
    @GetMapping("/{id}")
    public R<CvVendorTableField> getInfo(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        return R.ok(tableFieldService.queryById(id));
    }

    @SaCheckPermission("vendor:dimension:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvVendorTableFieldBo bo) {
        return toAjax(tableFieldService.insertByBo(bo));
    }

    @SaCheckPermission("vendor:dimension:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvVendorTableFieldBo bo) {
        return toAjax(tableFieldService.updateByBo(bo));
    }

    @SaCheckPermission("vendor:dimension:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "ids cannot be empty") @PathVariable Long[] ids) {
        return toAjax(tableFieldService.deleteByIds(List.of(ids)));
    }
}
