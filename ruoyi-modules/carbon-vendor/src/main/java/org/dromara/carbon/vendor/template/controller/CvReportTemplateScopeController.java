package org.dromara.carbon.vendor.template.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.template.domain.bo.CvReportTemplateScopeBo;
import org.dromara.carbon.vendor.template.domain.vo.CvReportTemplateScopeVo;
import org.dromara.carbon.vendor.template.service.ICvReportTemplateScopeService;
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
 * Vendor report template scope API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/report-template-scope")
public class CvReportTemplateScopeController extends BaseController {

    private final ICvReportTemplateScopeService reportTemplateScopeService;

    /**
     * List vendor report template scopes.
     */
    @SaCheckPermission("vendor:reportTemplateScope:list")
    @GetMapping("/list")
    public TableDataInfo<CvReportTemplateScopeVo> list(CvReportTemplateScopeBo reportTemplateScope, PageQuery pageQuery) {
        return reportTemplateScopeService.selectPageReportTemplateScopeList(reportTemplateScope, pageQuery);
    }

    /**
     * Get vendor report template scope details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:reportTemplateScope:query")
    @GetMapping("/{id}")
    public R<CvReportTemplateScopeVo> getInfo(@PathVariable Long id) {
        return R.ok(reportTemplateScopeService.selectReportTemplateScopeById(id));
    }

    /**
     * Add vendor report template scope.
     */
    @Log(title = "报告模板范围", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:reportTemplateScope:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvReportTemplateScopeBo reportTemplateScope) {
        return toAjax(reportTemplateScopeService.insertReportTemplateScope(reportTemplateScope));
    }

    /**
     * Edit vendor report template scope.
     */
    @Log(title = "报告模板范围", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:reportTemplateScope:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvReportTemplateScopeBo reportTemplateScope) {
        return toAjax(reportTemplateScopeService.updateReportTemplateScope(reportTemplateScope));
    }

    /**
     * Delete vendor report template scopes.
     *
     * @param ids primary keys
     */
    @Log(title = "报告模板范围", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:reportTemplateScope:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(reportTemplateScopeService.deleteReportTemplateScopeByIds(ids));
    }
}
