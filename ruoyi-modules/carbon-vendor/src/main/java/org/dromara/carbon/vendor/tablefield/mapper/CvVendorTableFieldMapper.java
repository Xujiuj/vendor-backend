package org.dromara.carbon.vendor.tablefield.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.tablefield.domain.CvVendorTableField;
import org.dromara.carbon.vendor.tablefield.domain.vo.CvVendorTableFieldVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvVendorTableFieldMapper extends BaseMapperPlus<CvVendorTableField, CvVendorTableFieldVo> {
}
