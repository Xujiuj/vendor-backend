package org.dromara.carbon.vendor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.open.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenDimensionRequest;
import org.dromara.carbon.vendor.service.ICvOpenDimensionService;
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
        return R.ok(openDimensionService.listDimensions(request));
    }
}
