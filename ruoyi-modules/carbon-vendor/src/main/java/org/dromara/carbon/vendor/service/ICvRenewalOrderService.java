package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvRenewalOrderBo;
import org.dromara.carbon.vendor.domain.vo.CvRenewalOrderVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor renewal order service.
 */
public interface ICvRenewalOrderService {

    TableDataInfo<CvRenewalOrderVo> selectPageRenewalOrderList(CvRenewalOrderBo bo, PageQuery pageQuery);

    CvRenewalOrderVo selectRenewalOrderById(Long id);

    int insertRenewalOrder(CvRenewalOrderBo bo);

    int updateRenewalOrder(CvRenewalOrderBo bo);

    int deleteRenewalOrderByIds(Long[] ids);
}
