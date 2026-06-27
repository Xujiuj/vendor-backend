package org.dromara.carbon.vendor.factor.controller;

import lombok.RequiredArgsConstructor;
import org.dromara.carbon.vendor.factor.domain.CvSourceAFactorImportResult;
import org.dromara.carbon.vendor.factor.service.ICvSourceAFactorImportService;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

/**
 * Vendor-side source(A) factor workbook import controller.
 *
 * @author Claude
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/vendor/source-a-factor")
public class CvSourceAFactorImportController extends BaseController {

    private final ICvSourceAFactorImportService sourceAFactorImportService;

    /**
     * Import factors from source(A) workbook.
     *
     * @param workbookPath absolute path to '2 排放因子表.xlsx'
     * @return import result
     */
    @PostMapping("/import")
    public R<CvSourceAFactorImportResult> importWorkbook(@RequestParam("workbookPath") String workbookPath) {
        return R.ok(sourceAFactorImportService.importWorkbook(Path.of(workbookPath)));
    }
}
