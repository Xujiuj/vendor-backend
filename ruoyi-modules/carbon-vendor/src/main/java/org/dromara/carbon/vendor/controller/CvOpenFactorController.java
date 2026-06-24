package org.dromara.carbon.vendor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenFactorSyncResponse;
import org.dromara.carbon.vendor.service.ICvOpenFactorService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * License-scoped vendor open factor API for enterprise backends.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/factors")
public class CvOpenFactorController {

    private final ICvOpenFactorService openFactorService;

    @GetMapping
    public R<CvOpenFactorSyncResponse> sync(@Valid CvOpenFactorSyncRequest request) {
        request.setLicenseId(CvOpenApiLicenseSupport.resolveLicenseId(request.getLicenseId()));
        return R.ok(openFactorService.syncFactors(request));
    }
}
