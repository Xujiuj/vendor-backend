package org.dromara.carbon.vendor.content.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.content.domain.bo.CvReportContentBo;
import org.dromara.carbon.vendor.content.domain.vo.CvReportContentVo;
import org.dromara.carbon.vendor.content.service.ICvReportContentService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
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

/**
 * Vendor report content catalog management API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/report-content")
public class CvReportContentController extends BaseController {

    private final ICvReportContentService reportContentService;

    @SaCheckPermission("vendor:reportContent:list")
    @GetMapping("/list")
    public TableDataInfo<CvReportContentVo> list(CvReportContentBo bo, PageQuery pageQuery) {
        return reportContentService.selectPageReportContentList(bo, pageQuery);
    }

    @SaCheckPermission("vendor:reportContent:query")
    @GetMapping("/{id}")
    public R<CvReportContentVo> getInfo(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        return R.ok(reportContentService.selectReportContentById(id));
    }

    @Log(title = "报表内容", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:reportContent:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvReportContentBo bo) {
        return toAjax(reportContentService.insertReportContent(bo));
    }

    @Log(title = "报表内容", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:reportContent:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvReportContentBo bo) {
        return toAjax(reportContentService.updateReportContent(bo));
    }

    @Log(title = "报表内容", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:reportContent:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "ids cannot be empty") @PathVariable Long[] ids) {
        return toAjax(reportContentService.deleteReportContentByIds(ids));
    }
}
