package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvLicenseIssueBo;
import org.dromara.carbon.vendor.domain.license.CvLicenseIssueRequest;
import org.dromara.carbon.vendor.domain.license.CvLicenseIssueResult;
import org.dromara.carbon.vendor.domain.license.CvLicenseReissueRequest;
import org.dromara.carbon.vendor.domain.license.CvLicenseRevokeRequest;
import org.dromara.carbon.vendor.domain.vo.CvLicenseIssueVo;
import org.dromara.carbon.vendor.service.ICvLicenseIssueService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor license issue read API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/license-issue")
public class CvLicenseIssueController extends BaseController {

    private final ICvLicenseIssueService licenseIssueService;

    /**
     * List vendor license issues.
     */
    @SaCheckPermission("vendor:licenseIssue:list")
    @GetMapping("/list")
    public TableDataInfo<CvLicenseIssueVo> list(CvLicenseIssueBo licenseIssue, PageQuery pageQuery) {
        return licenseIssueService.selectPageLicenseIssueList(licenseIssue, pageQuery);
    }

    /**
     * Get vendor license issue details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:licenseIssue:query")
    @GetMapping("/{id}")
    public R<CvLicenseIssueVo> getInfo(@PathVariable Long id) {
        return R.ok(licenseIssueService.selectLicenseIssueById(id));
    }

    /**
     * Issue a manual license file.
     */
    @Log(title = "许可证签发", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:licenseIssue:issue")
    @PostMapping("/issue")
    public R<CvLicenseIssueResult> issue(@Validated @RequestBody CvLicenseIssueRequest request) {
        request.setIssuedBy(resolveIssuedBy());
        CvLicenseIssueResult result = licenseIssueService.issueManualLicense(request);
        if (result.isIssued()) {
            return R.ok(result);
        }
        return R.fail(result.getStatus() + ": " + result.getMessage(), result);
    }

    /**
     * Reissue a revoked license file using append-only audit history.
     */
    @Log(title = "许可证签发", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:licenseIssue:issue")
    @PostMapping("/reissue")
    public R<CvLicenseIssueResult> reissue(@Validated @RequestBody CvLicenseReissueRequest request) {
        request.setIssuedBy(resolveIssuedBy());
        CvLicenseIssueResult result = licenseIssueService.reissueRevokedLicense(request);
        if (result.isIssued()) {
            return R.ok(result);
        }
        return R.fail(result.getStatus() + ": " + result.getMessage(), result);
    }

    /**
     * Revoke an issued license with vendor-side audit metadata.
     */
    @Log(title = "许可证签发", businessType = BusinessType.UPDATE)
    @SaCheckPermission("vendor:licenseIssue:revoke")
    @PostMapping("/revoke")
    public R<Void> revoke(@Validated @RequestBody CvLicenseRevokeRequest request) {
        request.setRevokedBy(resolveIssuedBy());
        return toAjax(licenseIssueService.revokeLicense(request));
    }

    private String resolveIssuedBy() {
        String username = LoginHelper.getUsername();
        return StringUtils.blankToDefault(username, "vendor-system");
    }
}
