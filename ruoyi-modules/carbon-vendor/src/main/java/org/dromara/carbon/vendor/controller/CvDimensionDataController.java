package org.dromara.carbon.vendor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.service.ICvDimensionDataService;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
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

import java.util.Arrays;
import java.util.Map;

/**
 * 统一维度数据 Controller
 * <p>按 dimensionCode 路由到对应的维度表，提供通用 CRUD 接口。</p>
 *
 * @author carbon
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/dimension-data")
public class CvDimensionDataController extends BaseController {

    private final ICvDimensionDataService dimensionDataService;

    /**
     * 分页查询维度数据列表
     */
    @SaCheckPermission("vendor:dimension:list")
    @GetMapping("/list")
    public TableDataInfo<?> list(@RequestParam String dimensionCode, PageQuery pageQuery) {
        return dimensionDataService.queryPageList(dimensionCode, pageQuery);
    }

    /**
     * 查询维度数据详情
     */
    @SaCheckPermission("vendor:dimension:list")
    @GetMapping("/{id}")
    public R<Map<String, Object>> getInfo(@RequestParam String dimensionCode,
                                          @NotNull(message = "id不能为空") @PathVariable Long id) {
        return R.ok(dimensionDataService.queryById(dimensionCode, id));
    }

    /**
     * 新增维度数据
     */
    @Log(title = "维度数据", businessType = BusinessType.INSERT)
    @SaCheckPermission("vendor:dimension:add")
    @PostMapping
    public R<Void> add(@RequestParam String dimensionCode,
                       @RequestBody Map<String, Object> bo) {
        return toAjax(dimensionDataService.insertByBo(dimensionCode, bo));
    }

    /**
     * 修改维度数据
     */
    @Log(title = "维度数据", businessType = BusinessType.UPDATE)
    @SaCheckPermission("vendor:dimension:edit")
    @PutMapping
    public R<Void> edit(@RequestParam String dimensionCode,
                        @RequestBody Map<String, Object> bo) {
        return toAjax(dimensionDataService.updateByBo(dimensionCode, bo));
    }

    /**
     * 批量删除维度数据
     */
    @Log(title = "维度数据", businessType = BusinessType.DELETE)
    @SaCheckPermission("vendor:dimension:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@RequestParam String dimensionCode,
                          @NotEmpty(message = "ids不能为空") @PathVariable Long[] ids) {
        return toAjax(dimensionDataService.deleteByIds(dimensionCode, Arrays.asList(ids)));
    }
}
