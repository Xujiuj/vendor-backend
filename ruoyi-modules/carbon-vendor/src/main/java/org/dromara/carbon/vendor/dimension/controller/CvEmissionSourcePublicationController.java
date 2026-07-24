package org.dromara.carbon.vendor.dimension.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.dimension.domain.bo.CvEmissionSourcePublicationBo;
import org.dromara.carbon.vendor.dimension.service.ICvEmissionSourcePublicationService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor-side publishing controls for 103 versions.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/emission-source-publication")
public class CvEmissionSourcePublicationController {

    private final ICvEmissionSourcePublicationService publicationService;

    @SaCheckPermission("vendor:dimension:list")
    @GetMapping
    public R<?> policy() {
        return R.ok(publicationService.queryPolicy());
    }

    @SaCheckPermission("vendor:dimension:list")
    @GetMapping("/versions")
    public R<?> versions() {
        return R.ok(publicationService.queryVersions());
    }

    @Log(title = "103版本发布", businessType = BusinessType.UPDATE)
    @SaCheckPermission("vendor:dimension:edit")
    @PostMapping
    public R<?> publish(@Valid @RequestBody CvEmissionSourcePublicationBo bo) {
        return R.ok(publicationService.publish(bo, StringUtils.blankToDefault(LoginHelper.getUsername(), "vendor-system")));
    }
}
