package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementListResponse;
import org.dromara.carbon.vendor.domain.open.CvOpenAnnouncementRequest;

/**
 * Vendor open announcement service.
 */
public interface ICvOpenAnnouncementService {

    /**
     * Return enabled vendor announcements for a licensed enterprise.
     *
     * @param request enterprise announcement request
     * @return enabled announcement list
     */
    CvOpenAnnouncementListResponse listAnnouncements(CvOpenAnnouncementRequest request);
}
