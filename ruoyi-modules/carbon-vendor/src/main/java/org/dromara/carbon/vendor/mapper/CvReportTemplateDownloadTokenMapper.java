package org.dromara.carbon.vendor.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.domain.CvReportTemplateDownloadToken;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor report template download token mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvReportTemplateDownloadTokenMapper
    extends BaseMapperPlus<CvReportTemplateDownloadToken, CvReportTemplateDownloadToken> {
}
