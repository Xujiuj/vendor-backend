package org.dromara.carbon.vendor.renewal.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.renewal.domain.CvRenewalOrder;
import org.dromara.carbon.vendor.renewal.domain.vo.CvRenewalOrderVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvRenewalOrderMapper extends BaseMapperPlus<CvRenewalOrder, CvRenewalOrderVo> {
}
