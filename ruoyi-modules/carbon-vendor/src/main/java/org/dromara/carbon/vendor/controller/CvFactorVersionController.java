package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorVersionVo;
import org.dromara.carbon.vendor.service.ICvFactorVersionService;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor factor version read API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/factor-version")
public class CvFactorVersionController extends BaseController {

    private final ICvFactorVersionService factorVersionService;

    /**
     * List vendor factor versions.
     */
    @SaCheckPermission("vendor:factorVersion:list")
    @GetMapping("/list")
    public TableDataInfo<CvFactorVersionVo> list(CvFactorVersionBo factorVersion, PageQuery pageQuery) {
        return factorVersionService.selectPageFactorVersionList(factorVersion, pageQuery);
    }

    /**
     * Get vendor factor version details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:factorVersion:query")
    @GetMapping("/{id}")
    public R<CvFactorVersionVo> getInfo(@PathVariable Long id) {
        return R.ok(factorVersionService.selectFactorVersionById(id));
    }
}
