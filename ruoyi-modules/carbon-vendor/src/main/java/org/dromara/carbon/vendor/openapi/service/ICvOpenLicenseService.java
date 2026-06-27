package org.dromara.carbon.vendor.openapi.service;

import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenLicenseCurrentResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderRequest;
import org.dromara.carbon.vendor.openapi.domain.CvOpenRenewalOrderResponse;

/**
 * License-scoped vendor open license API service.
 */
public interface ICvOpenLicenseService {

    CvOpenLicenseCurrentResponse currentLicense(CvOpenLicenseCurrentRequest request);

    CvOpenRenewalOrderResponse createRenewalOrder(CvOpenRenewalOrderRequest request);
}
