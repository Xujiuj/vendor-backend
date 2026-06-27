package org.dromara.carbon.vendor.openapi.service;

import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenDimensionRequest;

/**
 * Vendor open dimension service.
 */
public interface ICvOpenDimensionService {

    CvOpenDimensionListResponse listDimensions(CvOpenDimensionRequest request);
}
