package org.dromara.carbon.vendor.factor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.factor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorVersionVo;
import org.dromara.carbon.vendor.factor.service.ICvFactorVersionService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vendor factor version API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/factor-version")
public class CvFactorVersionController extends BaseController {

    private final ICvFactorVersionService factorVersionService;

    @SaCheckPermission("vendor:factorVersion:list")
    @GetMapping("/list")
    public TableDataInfo<CvFactorVersionVo> list(CvFactorVersionBo bo, PageQuery pageQuery) {
        return factorVersionService.selectPageFactorVersionList(bo, pageQuery);
    }

    @SaCheckPermission("vendor:factorVersion:query")
    @GetMapping("/{id}")
    public R<CvFactorVersionVo> getInfo(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        return R.ok(factorVersionService.selectFactorVersionById(id));
    }

    @Log(title = "因子版本", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorVersion:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CvFactorVersionBo bo) {
        return toAjax(factorVersionService.insertFactorVersion(bo));
    }

    @Log(title = "因子版本", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorVersion:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CvFactorVersionBo bo) {
        return toAjax(factorVersionService.updateFactorVersion(bo));
    }

    @Log(title = "因子版本", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:factorVersion:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "ids cannot be empty") @PathVariable Long[] ids) {
        return toAjax(factorVersionService.deleteFactorVersionByIds(ids));
    }

    @Log(title = "因子版本发布", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorVersion:publish")
    @PostMapping("/{id}/publish")
    public R<Void> publish(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        factorVersionService.releaseFactorVersion(id, resolveOperator());
        return R.ok();
    }

    @Log(title = "因子版本冻结", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorVersion:freeze")
    @PostMapping("/{id}/freeze")
    public R<Void> freeze(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        factorVersionService.freezeFactorVersion(id, resolveOperator());
        return R.ok();
    }

    @Log(title = "因子版本退役", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorVersion:retire")
    @PostMapping("/{id}/retire")
    public R<Void> retire(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        factorVersionService.retireFactorVersion(id, resolveOperator());
        return R.ok();
    }

    @Log(title = "因子版本恢复", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckPermission("vendor:factorVersion:restore")
    @PostMapping("/{id}/restore")
    public R<Void> restore(@NotNull(message = "id cannot be null") @PathVariable Long id) {
        factorVersionService.restoreFactorVersion(id, resolveOperator());
        return R.ok();
    }

    private String resolveOperator() {
        return StringUtils.blankToDefault(LoginHelper.getUsername(), "vendor-system");
    }
}
