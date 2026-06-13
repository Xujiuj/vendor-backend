package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateDownloadResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenReportTemplateRequest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Vendor open report template service.
 */
public interface ICvOpenReportTemplateService {

    /**
     * Return authorized report templates for a license.
     *
     * @param request enterprise sync request
     * @return authorized report template list
     */
    CvOpenReportTemplateListResponse listTemplates(CvOpenReportTemplateRequest request);

    /**
     * Return authorized report template download metadata.
     *
     * @param templateId report template id
     * @param request enterprise download request
     * @return authorized report template download metadata
     */
    CvOpenReportTemplateDownloadResponse downloadTemplate(Long templateId, CvOpenReportTemplateRequest request);

    /**
     * Consume a one-time token and stream the authorized report template file.
     *
     * @param downloadToken one-time download token
     * @param response servlet response
     * @throws IOException when streaming fails
     */
    void consumeDownloadToken(String downloadToken, HttpServletResponse response) throws IOException;
}
