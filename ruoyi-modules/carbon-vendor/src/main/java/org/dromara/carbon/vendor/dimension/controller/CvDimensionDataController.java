package org.dromara.carbon.vendor.dimension.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.dimension.service.ICvDimensionDataService;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/dimension-data")
public class CvDimensionDataController extends BaseController {

    private final ICvDimensionDataService dimensionDataService;

    @SaCheckPermission("vendor:dimension:list")
    @GetMapping("/list")
    public TableDataInfo<?> list(@RequestParam String dimensionCode, PageQuery pageQuery) {
        return dimensionDataService.queryPageList(dimensionCode, pageQuery);
    }

    @SaCheckPermission("vendor:dimension:query")
    @GetMapping("/{id}")
    public R<Map<String, Object>> getInfo(@NotNull(message = "id cannot be null") @PathVariable Long id,
                                          @RequestParam String dimensionCode) {
        return R.ok(dimensionDataService.queryById(dimensionCode, id));
    }

    @SaCheckPermission("vendor:dimension:add")
    @PostMapping
    public R<Void> add(@RequestParam String dimensionCode, @RequestBody Map<String, Object> bo) {
        return toAjax(dimensionDataService.insertByBo(dimensionCode, bo));
    }

    @SaCheckPermission("vendor:dimension:edit")
    @PutMapping
    public R<Void> edit(@RequestParam String dimensionCode, @RequestBody Map<String, Object> bo) {
        return toAjax(dimensionDataService.updateByBo(dimensionCode, bo));
    }

    @SaCheckPermission("vendor:dimension:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "ids cannot be empty") @PathVariable Long[] ids,
                          @RequestParam String dimensionCode) {
        return toAjax(dimensionDataService.deleteByIds(dimensionCode, List.of(ids)));
    }
}
