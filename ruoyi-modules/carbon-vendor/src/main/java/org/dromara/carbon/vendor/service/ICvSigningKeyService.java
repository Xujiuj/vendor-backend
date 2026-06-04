package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvSigningKeyBo;
import org.dromara.carbon.vendor.domain.vo.CvSigningKeyVo;
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
