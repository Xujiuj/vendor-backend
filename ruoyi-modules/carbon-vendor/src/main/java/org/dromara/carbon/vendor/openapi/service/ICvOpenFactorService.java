package org.dromara.carbon.vendor.openapi.service;

import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenFactorSyncResponse;

/**
 * Vendor open factor sync service.
 */
public interface ICvOpenFactorService {

    /**
     * Return the latest authorized factor version and records for a license.
     *
     * @param request enterprise sync request
     * @return authorized factor version and records
     */
    CvOpenFactorSyncResponse syncFactors(CvOpenFactorSyncRequest request);
}
