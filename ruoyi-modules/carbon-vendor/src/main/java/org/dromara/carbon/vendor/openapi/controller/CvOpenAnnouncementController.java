package org.dromara.carbon.vendor.openapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.openapi.domain.CvOpenAnnouncementListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenAnnouncementRequest;
import org.dromara.carbon.vendor.announcement.service.ICvOpenAnnouncementService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * License-scoped vendor open announcement API for enterprise backends.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/announcements")
public class CvOpenAnnouncementController {

    private final ICvOpenAnnouncementService openAnnouncementService;

    @GetMapping
    public R<CvOpenAnnouncementListResponse> list(@Valid CvOpenAnnouncementRequest request) {
        request.setLicenseId(CvOpenApiLicenseSupport.resolveLicenseId(request.getLicenseId()));
        return R.ok(openAnnouncementService.listAnnouncements(request));
    }
}
