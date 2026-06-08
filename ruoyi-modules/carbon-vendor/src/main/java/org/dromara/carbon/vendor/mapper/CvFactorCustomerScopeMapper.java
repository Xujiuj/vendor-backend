package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor factor customer scope mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvFactorCustomerScopeMapper extends BaseMapperPlus<CvFactorCustomerScope, CvFactorCustomerScopeVo> {
}
