package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvReportTemplateBo;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateUploadVo;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateVo;
import org.dromara.carbon.vendor.service.CvReportTemplateFileStorageService;
import org.dromara.carbon.vendor.service.ICvReportTemplateService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Vendor report template API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/report-template")
public class CvReportTemplateController extends BaseController {

    private final ICvReportTemplateService reportTemplateService;
    private final CvReportTemplateFileStorageService fileStorageService;

    /**
     * List vendor report templates.
     */
    @SaCheckPermission("vendor:reportTemplate:list")
    @GetMapping("/list")
    public TableDataInfo<CvReportTemplateVo> list(CvReportTemplateBo reportTemplate, PageQuery pageQuery) {
        return reportTemplateService.selectPageReportTemplateList(reportTemplate, pageQuery);
    }

    /**
     * Get vendor report template details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:reportTemplate:query")
    @GetMapping("/{id}")
    public R<CvReportTemplateVo> getInfo(@PathVariable Long id) {
        return R.ok(reportTemplateService.selectReportTemplateById(id));
    }

    /**
     * Add vendor report template.
     */
    @SaCheckPermission("vendor:reportTemplate:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvReportTemplateBo reportTemplate) {
        return toAjax(reportTemplateService.insertReportTemplate(reportTemplate));
    }

    /**
     * Upload a vendor-owned report template file and return local file metadata.
     */
    @SaCheckPermission(value = {
        "vendor:reportTemplate:add",
        "vendor:reportTemplate:edit"
    }, mode = SaMode.OR)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<CvReportTemplateUploadVo> upload(@RequestPart("file") MultipartFile file) {
        return R.ok(fileStorageService.store(file));
    }

    /**
     * Edit vendor report template.
     */
    @SaCheckPermission("vendor:reportTemplate:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvReportTemplateBo reportTemplate) {
        return toAjax(reportTemplateService.updateReportTemplate(reportTemplate));
    }

    /**
     * Delete vendor report templates.
     *
     * @param ids primary keys
     */
    @SaCheckPermission("vendor:reportTemplate:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(reportTemplateService.deleteReportTemplateByIds(ids));
    }

    /**
     * Publish vendor report template metadata.
     */
    @SaCheckPermission("vendor:reportTemplate:edit")
    @PostMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        reportTemplateService.publishReportTemplate(id, resolveOperator());
        return R.ok();
    }

    /**
     * Disable vendor report template metadata.
     */
    @SaCheckPermission("vendor:reportTemplate:edit")
    @PostMapping("/{id}/disable")
    public R<Void> disable(@PathVariable Long id) {
        reportTemplateService.disableReportTemplate(id, resolveOperator());
        return R.ok();
    }

    private String resolveOperator() {
        return StringUtils.blankToDefault(LoginHelper.getUsername(), "vendor-system");
    }
}
