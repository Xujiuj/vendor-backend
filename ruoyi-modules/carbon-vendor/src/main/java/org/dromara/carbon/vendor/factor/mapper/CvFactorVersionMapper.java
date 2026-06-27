package org.dromara.carbon.vendor.factor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.factor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.factor.domain.vo.CvFactorVersionVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvFactorVersionMapper extends BaseMapperPlus<CvFactorVersion, CvFactorVersionVo> {
}
