package org.dromara.carbon.vendor.dimension.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.dimension.domain.bo.CvDimensionRecordBo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvDimensionRecordVo;
import org.dromara.carbon.vendor.dimension.service.ICvDimensionRecordService;
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
 * Vendor dimension record API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/dimension-record")
public class CvDimensionRecordController extends BaseController {

    private final ICvDimensionRecordService dimensionRecordService;

    /**
     * List vendor dimension records.
     */
    @SaCheckPermission("vendor:dimension:list")
    @GetMapping("/list")
    public TableDataInfo<CvDimensionRecordVo> list(CvDimensionRecordBo bo, PageQuery pageQuery) {
        return dimensionRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * Get vendor dimension record details.
     */
    @SaCheckPermission("vendor:dimension:query")
    @GetMapping("/{id}")
    public R<CvDimensionRecordVo> getInfo(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        return R.ok(dimensionRecordService.queryById(id));
    }

    /**
     * Add vendor dimension record.
     */
    @Log(title = "维度记录", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:dimension:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvDimensionRecordBo bo) {
        return toAjax(dimensionRecordService.insertByBo(bo));
    }

    /**
     * Edit vendor dimension record.
     */
    @Log(title = "维度记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:dimension:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvDimensionRecordBo bo) {
        return toAjax(dimensionRecordService.updateByBo(bo));
    }

    /**
     * Delete vendor dimension records.
     */
    @Log(title = "维度记录", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:dimension:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "ids cannot be empty") @PathVariable Long[] ids) {
        return toAjax(dimensionRecordService.deleteByIds(ids));
    }
}
