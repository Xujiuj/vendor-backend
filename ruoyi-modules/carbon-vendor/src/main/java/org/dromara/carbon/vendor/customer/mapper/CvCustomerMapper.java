package org.dromara.carbon.vendor.customer.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.customer.domain.CvCustomer;
import org.dromara.carbon.vendor.customer.domain.vo.CvCustomerVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor customer archive mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvCustomerMapper extends BaseMapperPlus<CvCustomer, CvCustomerVo> {
}
