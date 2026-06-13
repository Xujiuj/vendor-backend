package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.vo.CvOverviewVo;
import org.dromara.carbon.vendor.service.ICvOverviewService;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor operations overview API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/overview")
public class CvOverviewController extends BaseController {

    private final ICvOverviewService overviewService;

    /**
     * Query vendor home page overview data.
     */
    @SaCheckLogin
    @GetMapping
    public R<CvOverviewVo> overview() {
        return R.ok(overviewService.queryOverview());
    }
}
