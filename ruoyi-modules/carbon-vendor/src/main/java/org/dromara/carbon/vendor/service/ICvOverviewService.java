package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.vo.CvOverviewVo;

/**
 * Vendor operations overview service.
 */
public interface ICvOverviewService {

    /**
     * Query live vendor operations overview data.
     *
     * @return overview data
     */
    CvOverviewVo queryOverview();
}
