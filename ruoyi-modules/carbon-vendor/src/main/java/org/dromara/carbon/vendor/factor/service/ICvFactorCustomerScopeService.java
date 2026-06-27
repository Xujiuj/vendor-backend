package org.dromara.carbon.vendor.factor.service;

import org.dromara.carbon.vendor.factor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor factor customer scope service.
 */
public interface ICvFactorCustomerScopeService {

    TableDataInfo<CvFactorCustomerScopeVo> selectPageFactorCustomerScopeList(CvFactorCustomerScopeBo bo, PageQuery pageQuery);

    CvFactorCustomerScopeVo selectFactorCustomerScopeById(Long id);

    int insertFactorCustomerScope(CvFactorCustomerScopeBo bo);

    int updateFactorCustomerScope(CvFactorCustomerScopeBo bo);

    boolean isFactorVersionAuthorized(Long versionId, Long customerId, Long packageId, String edition, String licenseId);

    int deleteFactorCustomerScopeByIds(Long[] ids);
}
