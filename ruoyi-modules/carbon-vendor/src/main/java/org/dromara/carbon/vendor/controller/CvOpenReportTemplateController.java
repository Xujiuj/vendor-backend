package org.dromara.carbon.vendor.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateDownloadResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateRequest;
import org.dromara.carbon.vendor.service.ICvOpenReportTemplateService;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * License-scoped vendor open report template API for enterprise backends.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/report-templates")
public class CvOpenReportTemplateController {

    private final ICvOpenReportTemplateService openReportTemplateService;

    @GetMapping
    public R<CvOpenReportTemplateListResponse> list(@Valid CvOpenReportTemplateRequest request) {
        request.setLicenseId(CvOpenApiLicenseSupport.resolveLicenseId(request.getLicenseId()));
        return R.ok(openReportTemplateService.listTemplates(request));
    }

    @GetMapping("/{id}/download")
    public R<CvOpenReportTemplateDownloadResponse> download(
        @PathVariable("id") Long id,
        @Valid CvOpenReportTemplateRequest request
    ) {
        request.setLicenseId(CvOpenApiLicenseSupport.resolveLicenseId(request.getLicenseId()));
        return R.ok(openReportTemplateService.downloadTemplate(id, request));
    }

    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP, message = "下载请求过于频繁，请稍后重试")
    @GetMapping("/download-tokens/{token}")
    public void consumeDownloadToken(
        @PathVariable("token") String token,
        HttpServletResponse response
    ) throws IOException {
        openReportTemplateService.consumeDownloadToken(token, response);
    }
}
