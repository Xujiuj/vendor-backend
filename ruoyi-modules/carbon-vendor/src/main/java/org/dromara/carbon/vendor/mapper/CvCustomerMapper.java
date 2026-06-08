package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvCustomer;
import org.dromara.carbon.vendor.domain.vo.CvCustomerVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor customer mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvCustomerMapper extends BaseMapperPlus<CvCustomer, CvCustomerVo> {
}
