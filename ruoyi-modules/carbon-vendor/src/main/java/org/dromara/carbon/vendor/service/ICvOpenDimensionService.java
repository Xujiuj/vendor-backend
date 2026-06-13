package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.open.CvOpenDimensionListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenDimensionRequest;

/**
 * Vendor open dimension service.
 */
public interface ICvOpenDimensionService {

    CvOpenDimensionListResponse listDimensions(CvOpenDimensionRequest request);
}
