package org.dromara.carbon.vendor.factor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.factor.domain.CvFactorCustomerScope;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorCustomerScopeVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvFactorCustomerScopeMapper extends BaseMapperPlus<CvFactorCustomerScope, CvFactorCustomerScopeVo> {
}
