package org.dromara.carbon.vendor.openapi.service;

import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenReportContentRequest;

/**
 * Vendor open report content catalog service.
 */
public interface ICvOpenReportContentService {

    CvOpenReportContentListResponse listContents(CvOpenReportContentRequest request);
}
