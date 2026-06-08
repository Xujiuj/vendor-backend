package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.domain.vo.CvRenewalOrderVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor renewal order mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvRenewalOrderMapper extends BaseMapperPlus<CvRenewalOrder, CvRenewalOrderVo> {
}
