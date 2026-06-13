package org.dromara.carbon.vendor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementRequest;
import org.dromara.carbon.vendor.service.ICvOpenAnnouncementService;
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
        return R.ok(openAnnouncementService.listAnnouncements(request));
    }
}
