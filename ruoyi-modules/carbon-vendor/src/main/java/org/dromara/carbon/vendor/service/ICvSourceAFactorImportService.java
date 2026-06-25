package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.CvSourceAFactorImportResult;

import java.nio.file.Path;

/**
 * Vendor-side source(A) factor workbook import service.
 *
 * @author Claude
 */
public interface ICvSourceAFactorImportService {

    /**
     * Import factors from source(A) workbook.
     *
     * @param workbookPath path to 2 排放因子表.xlsx
     * @return import result
     */
    CvSourceAFactorImportResult importWorkbook(Path workbookPath);
}
