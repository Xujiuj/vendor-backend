package org.dromara.carbon.vendor.openapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.openapi.service.ICvOpenDimensionService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * License-scoped vendor open dimension API for enterprise backends.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/dimensions")
public class CvOpenDimensionController {

    private final ICvOpenDimensionService openDimensionService;

    @GetMapping
    public R<CvOpenDimensionListResponse> list(@Valid CvOpenDimensionRequest request) {
        request.setLicenseId(CvOpenApiLicenseSupport.resolveLicenseId(request.getLicenseId()));
        return R.ok(openDimensionService.listDimensions(request));
    }
}
