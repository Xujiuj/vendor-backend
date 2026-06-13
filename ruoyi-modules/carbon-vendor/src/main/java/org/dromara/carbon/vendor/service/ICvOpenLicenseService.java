package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.open.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.domain.open.CvOpenRenewalOrderResponse;

/**
 * License-scoped vendor open license API service.
 */
public interface ICvOpenLicenseService {

    CvOpenLicenseCurrentResponse currentLicense(CvOpenLicenseCurrentRequest request);

    CvOpenRenewalOrderResponse createRenewalOrder(CvOpenRenewalOrderRequest request);
}
