package org.dromara.carbon.vendor.template.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.template.domain.CvReportTemplateScope;
import org.dromara.carbon.vendor.template.domain.vo.CvReportTemplateScopeVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor report template scope mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvReportTemplateScopeMapper extends BaseMapperPlus<CvReportTemplateScope, CvReportTemplateScopeVo> {
}
