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

    /**
     * Publish a draft factor version.
     *
     * @param id primary key
     * @param operatedBy operator identifier for audit metadata
     */
    void releaseFactorVersion(Long id, String operatedBy);

    /**
     * Freeze a published factor version.
     *
     * @param id primary key
     * @param operatedBy operator identifier for audit metadata
     */
    void freezeFactorVersion(Long id, String operatedBy);

    /**
     * Retire a published or frozen factor version.
     *
     * @param id primary key
     * @param operatedBy operator identifier for audit metadata
     */
    void retireFactorVersion(Long id, String operatedBy);

    /**
     * Restore a retired factor version back to draft.
     *
     * @param id primary key
     * @param operatedBy operator identifier for audit metadata
     */
    void restoreFactorVersion(Long id, String operatedBy);
}
