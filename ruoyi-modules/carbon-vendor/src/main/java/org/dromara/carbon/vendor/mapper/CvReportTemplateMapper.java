package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import org.dromara.carbon.vendor.domain.CvReportTemplate;
import org.dromara.carbon.vendor.domain.vo.CvReportTemplateVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor report template mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvReportTemplateMapper extends BaseMapperPlus<CvReportTemplate, CvReportTemplateVo> {
}
