package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.domain.bo.CvSigningKeyBo;
import org.dromara.carbon.vendor.domain.vo.CvSigningKeyVo;
import org.dromara.carbon.vendor.service.ICvSigningKeyService;
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
 * Vendor signing key API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/signing-key")
public class CvSigningKeyController extends BaseController {

    private final ICvSigningKeyService signingKeyService;

    /**
     * List vendor signing keys.
     */
    @SaCheckPermission("vendor:signingKey:list")
    @GetMapping("/list")
    public TableDataInfo<CvSigningKeyVo> list(CvSigningKeyBo signingKey, PageQuery pageQuery) {
        return signingKeyService.selectPageSigningKeyList(signingKey, pageQuery);
    }

    /**
     * Get vendor signing key details.
     *
     * @param id primary key
     */
    @SaCheckPermission("vendor:signingKey:query")
    @GetMapping("/{id}")
    public R<CvSigningKeyVo> getInfo(@PathVariable Long id) {
        return R.ok(signingKeyService.selectSigningKeyById(id));
    }
}
