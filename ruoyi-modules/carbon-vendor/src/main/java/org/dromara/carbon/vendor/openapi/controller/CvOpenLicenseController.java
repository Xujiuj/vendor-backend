package org.dromara.carbon.vendor.openapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderResponse;
import org.dromara.carbon.vendor.openapi.service.ICvOpenLicenseService;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * License-scoped vendor open license API for enterprise backends.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/open")
public class CvOpenLicenseController {

    private final ICvOpenLicenseService openLicenseService;

    @PostMapping("/licenses/current")
    public R<CvOpenLicenseCurrentResponse> current(@Valid @RequestBody CvOpenLicenseCurrentRequest request) {
        return R.ok(openLicenseService.currentLicense(request));
    }

    @PostMapping("/renewal-orders")
    public R<CvOpenRenewalOrderResponse> renewalOrder(@Valid @RequestBody CvOpenRenewalOrderRequest request) {
        return R.ok(openLicenseService.createRenewalOrder(request));
    }
}
