package org.dromara.carbon.vendor.service;

import org.dromara.carbon.vendor.domain.bo.CvCustomerBo;
import org.dromara.carbon.vendor.domain.vo.CvCustomerVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * Vendor customer service.
 */
public interface ICvCustomerService {

    /**
     * Query paged vendor customer list.
     *
     * @param bo customer query object
     * @param pageQuery pagination query
     * @return paged customer list
     */
    TableDataInfo<CvCustomerVo> selectPageCustomerList(CvCustomerBo bo, PageQuery pageQuery);

    /**
     * Query customer by id.
     *
     * @param id customer id
     * @return customer view object
     */
    CvCustomerVo selectCustomerById(Long id);
}
