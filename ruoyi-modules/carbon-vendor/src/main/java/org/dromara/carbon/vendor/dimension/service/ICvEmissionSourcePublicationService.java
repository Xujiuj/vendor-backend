package org.dromara.carbon.vendor.dimension.service;

import org.dromara.carbon.vendor.dimension.domain.bo.CvEmissionSourcePublicationBo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvEmissionSourcePublicationVo;
import org.dromara.carbon.vendor.dimension.domain.vo.CvEmissionSourceVersionVo;

import java.util.List;

/**
 * Authoritative publication scope for vendor 103 data.
 */
public interface ICvEmissionSourcePublicationService {

    CvEmissionSourcePublicationVo queryPolicy();

    List<CvEmissionSourceVersionVo> queryVersions();

    CvEmissionSourcePublicationVo publish(CvEmissionSourcePublicationBo bo, String operatedBy);

    void touchPublication();
}
