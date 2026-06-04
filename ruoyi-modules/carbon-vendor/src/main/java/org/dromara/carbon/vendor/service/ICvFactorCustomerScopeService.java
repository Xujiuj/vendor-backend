package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvFactorCustomerScopeBo;
import org.dromara.carbon.vendor.domain.vo.CvFactorCustomerScopeVo;
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

    int deleteFactorCustomerScopeByIds(Long[] ids);
}
