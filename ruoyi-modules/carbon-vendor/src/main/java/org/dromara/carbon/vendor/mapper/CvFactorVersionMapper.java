package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvFactorVersion;
import org.dromara.carbon.vendor.domain.vo.CvFactorVersionVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor factor version mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvFactorVersionMapper extends BaseMapperPlus<CvFactorVersion, CvFactorVersionVo> {
}
