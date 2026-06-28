package org.dromara.carbon.vendor.factor.service;

import org.dromara.carbon.vendor.factor.domain.bo.CvFactorVersionBo;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorVersionVo;
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
     * Create factor version metadata.
     *
     * @param bo factor version form object
     * @return whether insert succeeded
     */
    Boolean insertFactorVersion(CvFactorVersionBo bo);

    /**
     * Update factor version metadata.
     *
     * @param bo factor version form object
     * @return whether update succeeded
     */
    Boolean updateFactorVersion(CvFactorVersionBo bo);

    /**
     * Delete factor versions.
     *
     * @param ids primary keys
     * @return whether delete succeeded
     */
    Boolean deleteFactorVersionByIds(Long[] ids);

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
     * Unfreeze a frozen factor version back to published.
     *
     * @param id primary key
     * @param operatedBy operator identifier for audit metadata
     */
    void unfreezeFactorVersion(Long id, String operatedBy);

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
