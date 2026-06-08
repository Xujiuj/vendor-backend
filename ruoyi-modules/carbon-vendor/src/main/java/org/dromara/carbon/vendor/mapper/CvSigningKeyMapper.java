package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvSigningKey;
import org.dromara.carbon.vendor.domain.vo.CvSigningKeyVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor signing key mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvSigningKeyMapper extends BaseMapperPlus<CvSigningKey, CvSigningKeyVo> {
}
