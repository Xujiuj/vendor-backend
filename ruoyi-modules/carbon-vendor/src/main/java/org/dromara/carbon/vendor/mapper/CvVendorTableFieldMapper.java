package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.domain.CvVendorTableField;
import org.dromara.carbon.vendor.domain.vo.CvVendorTableFieldVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvVendorTableFieldMapper extends BaseMapperPlus<CvVendorTableField, CvVendorTableFieldVo> {
}
