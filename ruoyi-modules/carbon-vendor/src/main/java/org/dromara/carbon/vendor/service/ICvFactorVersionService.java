package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorVersionVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor factor version service.
 */
public interface ICvFactorVersionService {

    /**
     * Query paged factor version list.
     *
     * @param bo query object
     * @param pageQuery pagination query
     * @return paged factor version list
     */
    TableDataInfo<CvFactorVersionVo> selectPageFactorVersionList(CvFactorVersionBo bo, PageQuery pageQuery);

    /**
     * Query factor version by id.
     *
     * @param id primary key
     * @return factor version view object
     */
    CvFactorVersionVo selectFactorVersionById(Long id);
}
