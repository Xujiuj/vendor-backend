package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvFactorRecordBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorRecordVo;
import org.dromara.carbon.vendor.service.ICvFactorRecordService;
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
 * Vendor factor record API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/factor-record")
public class CvFactorRecordController extends BaseController {

    private final ICvFactorRecordService factorRecordService;

    /**
     * List vendor factor records.
     */
    @SaCheckPermission("vendor:factorRecord:list")
    @GetMapping("/list")
    public TableDataInfo<CvFactorRecordVo> list(CvFactorRecordBo factorRecord, PageQuery pageQuery) {
        return factorRecordService.selectPageFactorRecordList(factorRecord, pageQuery);
    }

    /**
     * Get vendor factor record details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:factorRecord:query")
    @GetMapping("/{id}")
    public R<CvFactorRecordVo> getInfo(@PathVariable Long id) {
        return R.ok(factorRecordService.selectFactorRecordById(id));
    }

    /**
     * Add vendor factor record.
     */
    @Log(title = "因子记录", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorRecord:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvFactorRecordBo factorRecord) {
        return toAjax(factorRecordService.insertFactorRecord(factorRecord));
    }

    /**
     * Edit vendor factor record.
     */
    @Log(title = "因子记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorRecord:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvFactorRecordBo factorRecord) {
        return toAjax(factorRecordService.updateFactorRecord(factorRecord));
    }

    /**
     * Delete vendor factor records.
     *
     * @param ids primary keys
     */
    @Log(title = "因子记录", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:factorRecord:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(factorRecordService.deleteFactorRecordByIds(ids));
    }
}
