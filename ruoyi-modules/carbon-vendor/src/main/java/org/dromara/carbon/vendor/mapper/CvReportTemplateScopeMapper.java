package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateScopeVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor report template scope mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvReportTemplateScopeMapper extends BaseMapperPlus<CvReportTemplateScope, CvReportTemplateScopeVo> {
}
