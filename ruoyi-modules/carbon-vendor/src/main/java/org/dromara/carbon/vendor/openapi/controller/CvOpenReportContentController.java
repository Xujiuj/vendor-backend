package org.dromara.carbon.vendor.openapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentRequest;
import org.dromara.carbon.vendor.openapi.service.ICvOpenReportContentService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * License-scoped vendor open report content catalog API for enterprise backends.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/report-contents")
public class CvOpenReportContentController {

    private final ICvOpenReportContentService openReportContentService;

    @GetMapping
    public R<CvOpenReportContentListResponse> list(@Valid CvOpenReportContentRequest request) {
        request.setLicenseId(CvOpenApiLicenseSupport.resolveLicenseId(request.getLicenseId()));
        return R.ok(openReportContentService.listContents(request));
    }
}
