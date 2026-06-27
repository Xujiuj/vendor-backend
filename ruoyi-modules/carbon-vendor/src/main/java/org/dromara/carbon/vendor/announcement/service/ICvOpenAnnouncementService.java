package org.dromara.carbon.vendor.announcement.service;

import org.dromara.carbon.vendor.openapi.domain.CvOpenAnnouncementListResponse;
import org.dromara.carbon.vendor.openapi.domain.CvOpenAnnouncementRequest;

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
