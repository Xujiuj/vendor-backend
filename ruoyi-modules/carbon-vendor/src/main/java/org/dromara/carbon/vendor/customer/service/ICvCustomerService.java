package org.dromara.carbon.vendor.customer.service;

import org.dromara.carbon.vendor.customer.domain.bo.CvCustomerBo;
import org.dromara.carbon.vendor.customer.domain.vo.CvCustomerVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * Vendor customer archive service.
 */
public interface ICvCustomerService {

    CvCustomerVo queryById(Long id);

    TableDataInfo<CvCustomerVo> queryPageList(CvCustomerBo bo, PageQuery pageQuery);

    List<CvCustomerVo> queryList(CvCustomerBo bo);

    Boolean insertByBo(CvCustomerBo bo);

    Boolean updateByBo(CvCustomerBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    boolean checkCustomerCodeUnique(CvCustomerBo bo);
}
