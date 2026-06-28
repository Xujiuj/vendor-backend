package org.dromara.carbon.vendor.content.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.dromara.carbon.vendor.content.domain.CvReportContent;
import org.dromara.carbon.vendor.content.domain.vo.CvReportContentVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * Vendor report content catalog mapper.
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface CvReportContentMapper extends BaseMapperPlus<CvReportContent, CvReportContentVo> {
}
