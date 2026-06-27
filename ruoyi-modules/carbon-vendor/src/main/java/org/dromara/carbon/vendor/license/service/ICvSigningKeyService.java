package org.dromara.carbon.vendor.license.service;

import org.dromara.carbon.vendor.license.domain.bo.CvSigningKeyBo;
import org.dromara.carbon.vendor.license.domain.vo.CvSigningKeyVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor signing key service.
 */
public interface ICvSigningKeyService {

    TableDataInfo<CvSigningKeyVo> selectPageSigningKeyList(CvSigningKeyBo bo, PageQuery pageQuery);

    CvSigningKeyVo selectSigningKeyById(Long id);

    int insertSigningKey(CvSigningKeyBo bo);

    int updateSigningKey(CvSigningKeyBo bo);

    int deleteSigningKeyByIds(Long[] ids);
}
